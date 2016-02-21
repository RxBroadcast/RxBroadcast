Vagrant.configure(2) do |config|
    config.vm.box = "ubuntu/trusty64"
    config.vm.synced_folder ".", "/vagrant", disabled: true
    config.vm.synced_folder ".", "/home/vagrant/workspace"
    config.vm.provision "docker"
    config.vm.provision "shell", name: "Install Travis CI Gem",
        inline: "{ apt-get -y install ruby-dev && gem install travis ; } &> /dev/null"
end
