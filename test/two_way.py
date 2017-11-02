import argparse
import colorama
import docker
import sys

from termcolor import colored
from test_utils import docker_interface, exec_shell_command


CONTAINERS = []
DEFAULT_TEST_ARGS = [
    '-Dport=8888',
    '-Ddestination=255.255.255.255',
]
NUM_TEST_CONTAINERS = 5


def new_container_from_client(client):
    def new_container(image, command=None, **kwargs):
        c = client.containers.run(image, command=command, **kwargs)
        CONTAINERS.append(c)
        return c
    return new_container

def test_args(*args):
    return DEFAULT_TEST_ARGS + list(args)

parser = argparse.ArgumentParser(description='RxBroadcast table tennis test script', add_help=False)
parser.add_argument('--help', action='help', help='show this help message and exit')
parser.add_argument('class_name', help='the fully qualified name of the test class to run')
parser.add_argument('network_command', help='a network command to apply to the JUnit container network interface')
args = parser.parse_args()
client = docker.from_env()
new_container = new_container_from_client(client)
colorama.init()
underlined = lambda s: colored(s, attrs=['underline'])

if len(client.images.list(name='rxb:latest')) != 1:
    print(colored('Attempting to build rxb image from the current working directory', 'yellow'))
    try:
        client.images.build(tag='rxb', path='.', rm=True)
        print(colored('Successfully build rxb image', 'green'))
    except docker.errors.BuildError:
        print(colored('Cannot find rxb image and building it failed', 'red'), file=sys.stderr)
        sys.exit(1)

print('Attempting to create {0} JUnit containers'.format(NUM_TEST_CONTAINERS))
for i in range(NUM_TEST_CONTAINERS):
    c = new_container('rxb', command=test_args('org.junit.runner.JUnitCore', args.class_name), detach=True)
    print(exec_shell_command(args.network_command, env={'DOCKER_IFACE': docker_interface(c).host}).decode(),)

new_container('rxb', command=test_args(args.class_name), detach=True)

errors = 0
for container in CONTAINERS:
    if container.wait() != 0:
        print(colored('{} errored'.format(container.short_id), 'red'))
        print(underlined('Logs'))
        print(container.logs().decode('utf-8'), file=sys.stderr)
        errors += 1
    else:
        print(colored('{} exited successfully'.format(container.short_id), 'green'))
        container.remove()

sys.exit(errors)
