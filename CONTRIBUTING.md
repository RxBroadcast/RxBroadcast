Contributing to RxBroadcast
===========================

By contributing to this project, you agree to license your contribution(s) under the terms outlined in the [`LICENSE`](LICENSE.md) file in the repository.

GitHub
------

All RxBroadcast development happens through GitHub. If you're new to GitHub, start with some of [their awesome guides](https://guides.github.com):

- [Contributing to Open Source on GitHub](https://guides.github.com/activities/contributing-to-open-source/)
- [Forking Projects](https://guides.github.com/activities/forking/)
- [Understanding the GitHub Flow](https://guides.github.com/introduction/flow/)

The build system
----------------

RxBroadcast uses Gradle as its build system. To build the project from the command line:

```bash
$ gradle build
```

To see a full list of Gradle tasks:

```bash
$ gradle tasks
```

Feature requests and bug reports
--------------------------------

Note that RxBroadcast is a library with an intentionally small feature set and API—it serves to describe a distributed event system. It is possible that you may want to wrap `Broadcast` objects or generally use them as building blocks.

To request a change to the way that RxBroadcast works, or report an issue with the library, [open an issue](https://github.com/RxBroadcast/RxBroadcast/issues). Whether or not something is a bug can be tricky to tell, please feel free to always open an issue even if you're unsure.

Vagrant
-------

Some of the more involved tests (i.e. the integration tests) use Docker to get multiple
networking stacks on the same machine. For the most part the Docker setup for those tests
are portable, but there is one complications: the IPv6 tests use the host's networking
stack (i.e. `--net=host`) and Docker for Mac does not support that feature (see
[docker/for-mac#1031][1]).

Included in the project is a `Vagrantfile` defining a Ubuntu virtual machine that has
Docker installed and that can be used when working with Docker. See [vagrantup.com][2]
for complete instructions, but a quick primer:

1. To create and SSH into the box run `vagrant up` and `vagrant ssh`
2. Ensure Docker was installed correctly via `docker --version`
3. The RxBroadcast repository is mounted into the `~/workspace` directory

  [1]:https://github.com/docker/for-mac/issues/1031
  [2]:https://www.vagrantup.com
  
### Running tests inside Vagrant

The Vagrant box is essentially a regular Linux workspace. Inside it you can run commands
similar to those run on CI. For example, running the integration tests:

```bash
$ cd ~/workspace
$ gradle testJar
$ scripts/test rxbroadcast.integration.NoOrderUdpBroadcastTest
```

(Double-check these commands with what's in `.travis.yml`—any of the test commands should be useful.)

### Using the Docker containers

There are a few entry points to the test JAR (the JAR produced by `gradle testJar`) that
are useful for local (or otherwise) testing. You can run these inside the Docker container
built from the project's `Dockerfile`.

The "ping-pong" tests can be run with two terminals, for example:

```bash
# In Terminal 1 and then in Terminal 2
docker run -it rxb \
    '-Ddestination=255.255.255.255' \
    '-Dport=8888' \
    'org.junit.runner.JUnitCore' \
    'rxbroadcast.integration.pp.PingPongUdpNoOrder'
```
