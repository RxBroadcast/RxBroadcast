Vagrant.configure(2) do |config|
    config.vm.box = "ubuntu/trusty64"
    config.vm.synced_folder ".", "/vagrant", disabled: true
    config.vm.synced_folder ".", "/home/vagrant/workspace"
    config.vm.provision "shell", name: "Install Docker",
        inline: "{ curl -fsSL https://get.docker.com/ | sh ; } &> /dev/null"
    config.vm.provision "shell", name: "Install Docker",
        inline: "{ sudo usermod -aG docker vagrant ; } &> /dev/null"
    config.vm.provision "shell", name: "Install Travis CI Gem",
        inline: "{ apt-get -y install ruby-dev && gem install travis ; } &> /dev/null"
end
