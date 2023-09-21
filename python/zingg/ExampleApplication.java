package py4j.examples;

import javax.net.SocketFactory;

import py4j.CallbackClient;
import py4j.GatewayServer;

public class ExampleApplication {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		GatewayServer.turnLoggingOff();
		GatewayServer server = new GatewayServer(new ExampleEntryPoint());
		server.start();
	}

	public static class ExampleNoMemManagementApplication {
		public static void main(String[] args) {
			GatewayServer.turnLoggingOff();
			CallbackClient callbackClient = new CallbackClient(GatewayServer.DEFAULT_PYTHON_PORT,
					GatewayServer.defaultAddress(), CallbackClient.DEFAULT_MIN_CONNECTION_TIME,
					CallbackClient.DEFAULT_MIN_CONNECTION_TIME_UNIT, SocketFactory.getDefault(), false);
			GatewayServer server = new GatewayServer(new ExampleEntryPoint(), GatewayServer.DEFAULT_PORT,
					GatewayServer.defaultAddress(), GatewayServer.DEFAULT_CONNECT_TIMEOUT,
					GatewayServer.DEFAULT_READ_TIMEOUT, null, callbackClient);
			server.start();
		}
	}

	public static class ExamplePythonEntryPointApplication {

		public static void main(String[] args) {
			String authToken = null;
			if (args.length > 0) {
				authToken = args[0];
			}
			GatewayServer.turnLoggingOff();
			GatewayServer server = new GatewayServer.GatewayServerBuilder()
					.callbackClient(GatewayServer.DEFAULT_PYTHON_PORT, GatewayServer.defaultAddress(), authToken)
					.build();
			server.start();
			IHello hello = (IHello) server.getPythonServerEntryPoint(new Class[] { IHello.class });
			try {
				hello.sayHello();
				hello.sayHello(2, "Hello World");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static class ExampleShortTimeoutApplication {
		public static void main(String[] args) {
			GatewayServer.turnLoggingOff();
			CallbackClient callbackClient = new CallbackClient(GatewayServer.DEFAULT_PYTHON_PORT,
					GatewayServer.defaultAddress(), CallbackClient.DEFAULT_MIN_CONNECTION_TIME,
					CallbackClient.DEFAULT_MIN_CONNECTION_TIME_UNIT, SocketFactory.getDefault(), false, 250);
			GatewayServer server = new GatewayServer.GatewayServerBuilder().readTimeout(250)
					.entryPoint(new ExampleEntryPoint()).callbackClient(callbackClient).build();
			server.start();

		}
	}

	public static class ExampleIPv6Application {
		public static void main(String[] args) {
			GatewayServer.turnLoggingOff();
			CallbackClient callbackClient = new CallbackClient(GatewayServer.DEFAULT_PYTHON_PORT,
					GatewayServer.defaultIPv6Address(), CallbackClient.DEFAULT_MIN_CONNECTION_TIME,
					CallbackClient.DEFAULT_MIN_CONNECTION_TIME_UNIT, SocketFactory.getDefault(), false, 250);
			GatewayServer server = new GatewayServer.GatewayServerBuilder().readTimeout(250)
					.entryPoint(new ExampleEntryPoint()).callbackClient(callbackClient)
					.javaAddress(GatewayServer.defaultIPv6Address()).build();
			server.start();

		}
	}

}