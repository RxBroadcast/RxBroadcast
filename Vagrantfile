Vagrant.configure(2) do |config|
    config.vm.box = "ubuntu/trusty64"
    config.vm.synced_folder ".", "/vagrant", disabled: true
    config.vm.synced_folder ".", "/home/vagrant/workspace"
    (1..4).each do |i|
        config.vm.define "m#{i}" do |config|
            config.vm.hostname = "m#{i}"
            config.vm.provision "shell", privileged: false, inline: %(
                if [[ ! -d .files ]]
                then
                    mkdir .files
                    curl -sL https://git.io/vuHfs | tar -xz -C .files --strip-components=1
                    VAGRANT=true .files/install 2>&1 &> /dev/null
                fi
                sudo add-apt-repository -y ppa:cwchien/gradle
                sudo apt-get -qqy update
                sudo apt-get -qqy dist-upgrade
                sudo apt-get -qqy install \
                    gradle-2.10 \
                    htop \
                    openjdk-8-jdk \
                    # END PACKAGE LIST
            )
            config.vm.provider "virtualbox" do |virtualbox|
                virtualbox.linked_clone = true
            end
        end
    end
end
