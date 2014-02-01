/*
 * Copyright 2002-2003, Wade Wassenberg  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 */

package x10.net;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import x10.Command;
import x10.Controller;
import x10.UnitEvent;
import x10.UnitListener;
import x10.util.ThreadSafeQueue;

/**
 * ServerDispatchProxy is the server-side proxy which dispatches events to and
 * receives commands from the SocketController assigned to this object. A new
 * ServerDispatchProxy is constructed for each SocketController that connects to
 * a ControllerServer.
 * 
 * @author Wade Wassenberg
 * 
 * @version 1.0
 */
public class ServerDispatchProxy extends Thread implements UnitListener {
	private static final Logger logger = LogManager.getLogger();
	private final Controller c;
	private final ObjectOutputStream oos;
	private final ObjectInputStream ois;

	/**
	 * s Socket the Socket connection to the client SocketController
	 */
	Socket s;
	private volatile boolean alive;
	private final ThreadSafeQueue commandQueue;

	/**
	 * ServerDispatchProxy constructs a new ServerDispatchProxy for the
	 * specified Controller to the SocketController on the other end of the
	 * specified Socket.
	 * 
	 * @param s
	 *            the Socket to the connected SocketController
	 * @param c
	 *            the Controller that is being shared across the network with
	 *            the SocketController.
	 * @exception IOException
	 *                if an error occurs obtaining streams to/from the connected
	 *                SocketController.
	 */
	public ServerDispatchProxy(Socket s, Controller c) throws IOException {
		this.s = s;
		this.c = c;
		oos = new ObjectOutputStream(s.getOutputStream());
		ois = new ObjectInputStream(s.getInputStream());
		commandQueue = new ThreadSafeQueue();
		start();
	}

	/**
	 * run reads Commands from the SocketController and propagates them along to
	 * the local Controller object.
	 */
	@Override
	public void run() {
		logger.info("ServerDispatchProxy running");
		alive = true;
		while (alive) {
			try {
				logger.info("blocking read...");
				Command nextCommand = (Command) ois.readObject();
				logger.info("Command recieved from client");
				commandQueue.enqueue(nextCommand);
				c.addCommand(nextCommand);
			} catch (IOException e) {
				logger.catching(e);
				alive = false;
			} catch (ClassNotFoundException e) {
				logger.catching(e);
			}
		}
		c.removeUnitListener(this);
	}

	/**
	 * dispatchEvent sends all events received locally to the SocketController
	 * that is connected.
	 * 
	 * @param event
	 *            the event to dispatch to the SocketController
	 */
	private synchronized void dispatchEvent(UnitEvent event) {
		if (event.getCommand() == commandQueue.peek()) {
			commandQueue.dequeue();
		} else {
			try {
				oos.writeObject(event);
				oos.flush();
				logger.info("Event written to client");
			} catch (Exception e) {
				logger.catching(e);
			}
		}
	}

	/**
	 * allUnitsOff receives the UnitListener event to be dispatched to the
	 * SocketController
	 * 
	 * @param event
	 *            the event to be dispatched
	 */
	@Override
	public void allUnitsOff(UnitEvent event) {
		dispatchEvent(event);
	}

	/**
	 * allLightsOff receives the UnitListener event to be dispatched to the
	 * SocketController
	 * 
	 * @param event
	 *            the event to be dispatched
	 */

	@Override
	public void allLightsOff(UnitEvent event) {
		dispatchEvent(event);
	}

	/**
	 * allLightsOn receives the UnitListener event to be dispatched to the
	 * SocketController
	 * 
	 * @param event
	 *            the event to be dispatched
	 */
	@Override
	public void allLightsOn(UnitEvent event) {
		dispatchEvent(event);
	}

	/**
	 * unitOn receives the UnitListener event to be dispatched to the
	 * SocketController
	 * 
	 * @param event
	 *            the event to be dispatched
	 */
	@Override
	public void unitOn(UnitEvent event) {
		dispatchEvent(event);
	}

	/**
	 * unitOff receives the UnitListener event to be dispatched to the
	 * SocketController
	 * 
	 * @param event
	 *            the event to be dispatched
	 */
	@Override
	public void unitOff(UnitEvent event) {
		dispatchEvent(event);
	}

	/**
	 * unitDim receives the UnitListener event to be dispatched to the
	 * SocketController
	 * 
	 * @param event
	 *            the event to be dispatched
	 */
	@Override
	public void unitDim(UnitEvent event) {
		dispatchEvent(event);
	}

	/**
	 * unitBright receives the UnitListener event to be dispatched to the
	 * SocketController
	 * 
	 * @param event
	 *            the event to be dispatched
	 */
	@Override
	public void unitBright(UnitEvent event) {
		dispatchEvent(event);
	}

}