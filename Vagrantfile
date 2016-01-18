Vagrant.configure(2) do |config|
    config.vm.box = "ubuntu/vivid64"
    config.vm.synced_folder ".", "/vagrant", disabled: true
    config.vm.synced_folder ".", "/home/vagrant/workspace"
    (1..4).each do |i|
        config.vm.define "m#{i}" do |config|
            config.vm.hostname = "m#{i}"
            config.vm.provider "virtualbox" do |virtualbox|
                virtualbox.linked_clone = true
            end
        end
    end
end
