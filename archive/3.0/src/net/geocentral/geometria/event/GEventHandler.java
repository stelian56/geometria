/**
 * Copyright 2000-2010 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License
 * http://www.gnu.org/licenses
 */
package net.geocentral.geometria.event;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EventObject;

public class GEventHandler {

    private Collection<GEventListener> listeners;

    public GEventHandler() {
        listeners = new ArrayList<GEventListener>();
    }

    public void addListener(GEventListener listener) {
        listeners.add(listener);
    }

    public void fireEvent(EventObject event) {
        for (GEventListener listener : listeners)
            listener.handleEvent(event);
    }
}
