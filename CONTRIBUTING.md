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
