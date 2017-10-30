package rxbroadcast;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;

public final class WhoAmITest {
    @SuppressWarnings({"checkstyle:MagicNumber"})
    @Test
    public final void sendRecvDoesCloseItsSocket() throws IOException {
        final WhoAmI whoAmI = new WhoAmI(12345, false);
        final DatagramSocket socket = new DatagramSocket();

        whoAmI.sendRecv(socket);

        Assert.assertTrue(socket.isClosed());
    }

    @Test
    public final void ipv4DoesResultInIpv4Address() throws IOException {
        final boolean isIPv6 = false;
        final WhoAmI whoAmI = new WhoAmI(12345, isIPv6);
        final DatagramSocket socket = new DatagramSocket();

        final DatagramPacket packet = whoAmI.sendRecv(socket);

        Assert.assertTrue(packet.getAddress() instanceof Inet4Address);
    }
}
