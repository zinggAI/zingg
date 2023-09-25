import py4j.GatewayServer;
public class TestPy4JGateway {

    public static void main(String[] args) {
        GatewayServer gatewayServer = new GatewayServer(new Object());
        gatewayServer.start();
        System.out.println("Gateway Server Started");
    }
    
}
