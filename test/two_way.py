import argparse
import colorama
import docker
import signal
import sys

from termcolor import colored
from test_utils import build_image_if_not_exists, \
    docker_interface, \
    exec_shell_command


CONTAINERS = []
DEFAULT_IMAGE_NAME = 'rxb'
DEFAULT_SOURCE_PORT = 8000
DEFAULT_DEST_PORT = DEFAULT_SOURCE_PORT
DEFAULT_NUM_TEST_CONTAINERS = 5


def new_container_from_client(client, **default_kwargs):
    def merge_dictionaries(x, y):
        z = x.copy()
        z.update(y)
        return z

    def new_container(image, command=None, **kwargs):
        c = client.containers.run(image, command=command, **merge_dictionaries(default_kwargs, kwargs))
        CONTAINERS.append(c)
        return c
    return new_container


def underlined(s):
    return colored(s, attrs=['underline'])


def stop_all_containers():
    print()
    print(colored('Received ^C, killing all containers before exit', 'yellow'))
    for c in filter(lambda x: x.status != 'exited', CONTAINERS):
        c.kill()
    sys.exit(0)


signal.signal(signal.SIGTERM, stop_all_containers)
parser = argparse.ArgumentParser(description='RxBroadcast table tennis test script', add_help=False)
parser.add_argument('--help', action='help', help='show this help message and exit')
parser.add_argument('--image', default=DEFAULT_IMAGE_NAME, help='the Docker image name to use for the containers')
parser.add_argument('--ipv6', action='store_true', help='use IPv6 for the network addresses')
parser.add_argument('--no-remove', action='store_true', help="don't remove successful containers")
parser.add_argument('--num-junit-containers', default=DEFAULT_NUM_TEST_CONTAINERS, type=int, help='the number of JUnit containers to create')
parser.add_argument('class_name', help='the fully qualified name of the test class to run')
parser.add_argument('network_command', nargs='?', default=':', help='a network command to apply to the JUnit container network interface')
args = parser.parse_args()

docker_run_opts = dict()
source_port = DEFAULT_SOURCE_PORT
dest_port = DEFAULT_DEST_PORT
destination = '255.255.255.255'
num_junit_containers = args.num_junit_containers
if args.ipv6:
    docker_run_opts['network_mode'] = 'host'
    destination = 'ff02::1'
    # Where we're using the host networking stack we now need to
    # have different source and destination ports
    source_port = source_port - 1
    dest_port = dest_port + 1
    if num_junit_containers > 1:
        print(colored('Constraining JUnit container count to 1', 'yellow'))
        num_junit_containers = 1

junit_args = [
    '-Dport={0}'.format(dest_port),
    '-DdestinationPort={0}'.format(source_port),
    '-Ddestination={0}'.format(destination),
    'org.junit.runner.JUnitCore',
    args.class_name,
]
main_class_args = [
    '-Dport={0}'.format(source_port),
    '-DdestinationPort={0}'.format(dest_port),
    '-Ddestination={0}'.format(destination),
    args.class_name,
]
client = docker.from_env()
new_container = new_container_from_client(client, **docker_run_opts)
colorama.init()

if len(client.images.list(name='{0.image}:latest'.format(args))) != 1:
    build_image_if_not_exists(client, args.image)

print('Attempting to create {0} JUnit containers'.format(num_junit_containers))
for i in range(num_junit_containers):
    c = new_container(args.image, command=junit_args, detach=True)
    print(exec_shell_command(args.network_command, env={'DOCKER_IFACE': docker_interface(c).host}).decode(), end='')

new_container(args.image, command=main_class_args, detach=True)

errors = 0
try:
    for container in CONTAINERS:
        if container.wait() != 0:
            print(colored('{} errored'.format(container.short_id), 'red'))
            print(underlined('Logs'))
            print(container.logs().decode('utf-8'), file=sys.stderr)
            errors += 1
        else:
            print(colored('{} exited successfully'.format(container.short_id), 'green'))
            if not args.no_remove:
                container.remove()
except KeyboardInterrupt:
    stop_all_containers()

sys.exit(errors)
