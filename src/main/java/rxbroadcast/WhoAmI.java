package rxbroadcast;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.Callable;

final class WhoAmI implements Callable<Sender> {
    @SuppressWarnings("PMD.AvoidUsingHardCodedIP")
    private static final String IPV6_ALL_NODES_MULTICAST = "ff02::1";

    @SuppressWarnings("PMD.AvoidUsingHardCodedIP")
    private static final String IPV4_BROADCAST = "255.255.255.255";

    private final boolean ipv6;

    private final int port;

    WhoAmI(final int port, final boolean ipv6) {
        this.port = port;
        this.ipv6 = ipv6;
    }

    @NotNull
    @Override
    public final Sender call() throws IOException {
        // Listen on all interfaces, random port
        final DatagramSocket ws = new DatagramSocket();
        final DatagramPacket recvPacket = sendRecv(ws);
        return new Sender(recvPacket.getAddress(), port);
    }

    @SuppressWarnings("checkstyle:AvoidInlineConditionals")
    @NotNull
    final DatagramPacket sendRecv(@NotNull final DatagramSocket ws) throws IOException {
        // Send WHO_AM_I packet
        final byte[] data = new byte[]{0x42};
        final InetAddress address = InetAddress.getByName(ipv6 ? IPV6_ALL_NODES_MULTICAST : IPV4_BROADCAST);
        final DatagramPacket packet = new DatagramPacket(data, data.length, address, ws.getLocalPort());
        ws.send(packet);
        // Attempt to receive the WHO_AM_I packet
        final DatagramPacket recvPacket = new DatagramPacket(new byte[1], 1);
        ws.receive(recvPacket);
        // Whoever sent it is us
        ws.close();
        return recvPacket;
    }
}
