import collections
import docker
import os
import re
import shlex
import subprocess
import sys

from termcolor import colored


DockerInterface = collections.namedtuple('DockerInterface', ['host', 'container'])


def build_image_if_not_exists(client, image_name, exit_code=1):
    print(colored('Attempting to build {0} image from the current working directory'.format(image_name), 'yellow'))
    try:
        client.images.build(tag=image_name, path=os.getcwd(), rm=True)
        print(colored('Built {0} image'.format(image_name), 'green'))
    except docker.errors.BuildError:
        print(colored('Cannot find {0} image and building it failed'.format(image_name), 'red'), file=sys.stderr)
        sys.exit(exit_code)


def docker_interface(container):
    # Inside the container we have our eth0 interface with an arbitrary index assigned to it:
    #
    # >>> 8: eth0: <BROADCAST,MULTICAST,UP,LOWER_UP> mtu 1500 qdisc noqueue state UP
    # >>>     link/ether 02:42:ac:11:00:02 brd ff:ff:ff:ff:ff:ff
    # On the host we end up with something like so:
    #
    # >>> 9: veth862e67b: <BROADCAST,MULTICAST,UP,LOWER_UP> mtu 1500 qdisc noqueue master docker0 state UP mode DEFAULT group default
    # >>>     link/ether 72:be:31:98:2e:6e brd ff:ff:ff:ff:ff:ff
    #
    # Apparently it's just a coincidence that 9 is (8 + 1) but let's depend on that and hate ourselves
    # in the future when it breaks.
    def ip_link_list():
        return subprocess.check_output(shlex.shlex('ip link list'))

    def eth0_interface_index(container):
        return int(container.exec_run('ip link list eth0').split(b':')[0])

    def interface_for_container(container_eth0_index):
        host_interface_index = container_eth0_index + 1
        interfaces = re.findall(
            '(?<={}[:][ ])[^:]+(?=[:])'.format(host_interface_index).encode(),
            ip_link_list())
        if not interfaces:
            raise Exception(
                'The host is missing an interface indexed as {}'.format(
                    host_interface_index))
        return interfaces[0]

    return DockerInterface(
        host=interface_for_container(eth0_interface_index(container)),
        container='eth0')


def exec_shell_command(command, env=None):
    def merge_dictionaries(x, y):
        z = x.copy()
        z.update(y)
        return z
    if env is None:
        env = dict()
    return subprocess.check_output("bash -x -c 'shopt -s extglob; {}'".format(command), env=merge_dictionaries(os.environ, env), shell=True)
