import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;



public class Responder {

    public void openConnectionToTestServerForHTTPData(  ) {
        Socket socket = new Socket();
        try {
            socket.connect(new InetSocketAddress( 1776));
           // Connection connection = handleNewSocket(socket);
            JSONObject identifierObject = new JSONObject();
            try {
                identifierObject.put("messageType", "clarelink_uri_data");
                    identifierObject.put("uri","/test1.html");
            } catch (JSONException e) {
                System.out.println(e.getClass().getName() + ": " + e.getMessage());
            }
            byte[] data = identifierObject.toString().concat("\n").getBytes();
            byte[]size = int2bytes(data.length);
            socket.getOutputStream().write(size);
            socket.getOutputStream().write(data);
            socket.getOutputStream().flush();
            
            Thread sendThread = new SendThread(socket);
            sendThread.start();
            
        } catch (IOException e) {
            System.out.println(e.getClass().getName() + ": " + e.getMessage());
            if ( socket != null && !socket.isClosed()){
                try {
                    socket.close();
                } catch (IOException e1) {
                    System.out.println("munch");
                }
            }
        }
    }
        
        
         
        
        
        
        /* byte, in an endian format that the ipad can use without conversion */
        /* not standard ! */
        private static final int BYTES_PER_INT = 4;
        private static final int BYTE_LENGTH = 8;
        private static final int BYTE_MASK = 0x00ff;
        private final byte[] int2bytes(int anInt) {
            byte[] normalizedByteArray = new byte[BYTES_PER_INT];
            int position = 0;
            normalizedByteArray[position] = (byte) ((anInt >> position * BYTE_LENGTH) & BYTE_MASK);
            normalizedByteArray[++position] = (byte) ((anInt >> position * BYTE_LENGTH) & BYTE_MASK);
            normalizedByteArray[++position] = (byte) ((anInt >> position * BYTE_LENGTH) & BYTE_MASK);
            normalizedByteArray[++position] = (byte) ((anInt >> position * BYTE_LENGTH) & BYTE_MASK);
            return normalizedByteArray;
        }
        
        
    class SendThread extends Thread {
        private final Socket socket;

        /**
         * Constructor.
         * 
         * @param uriPort
         */
        public SendThread(Socket socket) {
            super("sendThread for " + socket.getRemoteSocketAddress());

            this.socket = socket;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Thread#run()
         */
        @Override
        public void run() {

            URL x;
            try {
                x = new URL("http://i.cdn.turner.com/cnn/.e/img/3.0/global/header/hdr-main.gif");

                HttpURLConnection connection = (HttpURLConnection) x.openConnection();

                StringBuffer strBuff = new StringBuffer();
                int i = 1;
                String header = connection.getHeaderField(0);
                strBuff.append(header);
                while ((header = connection.getHeaderField(i)) != null) {
                    String key = connection.getHeaderFieldKey(i);
                    strBuff.append("\r\n");
                    strBuff.append(((key == null) ? "" : key + ": ") + header);
                    i++;
                }
                strBuff.append("\r\n");
                strBuff.append("\r\n");
                System.out.println("Constructed header:\r\n  " + strBuff);
                socket.getOutputStream().write(strBuff.toString().getBytes());
                socket.getOutputStream().flush();

                // socket.getOutputStream().write("helloWorld".getBytes());
                // socket.getOutputStream().flush();

                InputStream stream = connection.getInputStream();
                byte[] byteChunk = new byte[4096];
                int readsize = -1;
                // int debugCounter = 0;
                while ((readsize = stream.read(byteChunk)) > 0) {
                    socket.getOutputStream().write(byteChunk, 0, readsize);
                    // debugCounter+=readsize;
                }
                // LOG.info("bytes written: "+debugCounter);
                socket.getOutputStream().flush();
                socket.close();
                connection.disconnect();
            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
    }



    
    public static void main(String[] args) {
        new Responder().openConnectionToTestServerForHTTPData();

    }

}
