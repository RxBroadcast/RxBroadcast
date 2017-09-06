package rxbroadcast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.Callable;

final class WhoAmI implements Callable<Sender> {
    private final int destinationPort;

    WhoAmI(final int destinationPort) {
        this.destinationPort = destinationPort;
    }

    @Override
    public final Sender call() throws IOException {
        // Listen all all interfaces, random port
        final DatagramSocket ws = new DatagramSocket();
        // Send WHO_AM_I packet
        final byte[] data = new byte[]{0x42};
        @SuppressWarnings("PMD.AvoidUsingHardCodedIP")
        final DatagramPacket packet = new DatagramPacket(
            data, data.length, InetAddress.getByName("255.255.255.255"), ws.getLocalPort());
        ws.send(packet);
        // Attempt to receive the WHO_AM_I packet
        final DatagramPacket recvPacket = new DatagramPacket(new byte[1], 1);
        ws.receive(recvPacket);
        // Whoever sent it is us
        ws.close();
        return new Sender(recvPacket.getAddress(), destinationPort);
    }
}
