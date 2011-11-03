package org.ijsberg.iglu.runtime.module;

public class Environment extends Starter implements Runnable {

	private Thread shutdownHook;
	private boolean isRunning;

	public Environment() {
		super();
		shutdownHook = new Thread(new ShutdownProcess());
		Runtime.getRuntime().addShutdownHook(shutdownHook);
	}


	/**
	 * Performs a (forced) shutdown sequence.
	 */
	private class ShutdownProcess implements Runnable {

		/**
		 * Invokes shutdown when startup is completed.
		 */
		public void run() {
			System.out.println("starting " + (isRunning ? "forced" : "")
					+ " application shutdown process...");
			if (isStarted) {
				try {
					stop();
				} catch (Throwable t) {
					t.printStackTrace();
				}
			}
			System.out
					.println("Application shutdown process completed...");
		}
	}

	@Override
	public synchronized void start() {
		super.start();
		if (!isRunning) {
			new Thread(this).start();
		}
	}

	@Override
	public synchronized void stop() {
		super.stop();
		isRunning = false;
	}


	public void run() {
		isRunning = true;
		while (isRunning) {
			try {
				Thread.sleep(333);
			} catch (InterruptedException e) {
				if (isStarted) {
					System.out
							.println("Keep-alive thread interrupted...");
					stop();
				}
			}
		}
	}

}
