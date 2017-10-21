package rxbroadcast;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.DatagramSocket;

public final class WhoAmITest {
    @SuppressWarnings({"checkstyle:MagicNumber"})
    @Test
    public final void sendRecvDoesCloseItsSocket() throws IOException {
        final WhoAmI whoAmI = new WhoAmI(12345);
        final DatagramSocket socket = new DatagramSocket();

        whoAmI.sendRecv(socket);

        Assert.assertTrue(socket.isClosed());
    }
}
