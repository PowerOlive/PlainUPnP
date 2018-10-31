/**
 * Copyright (C) 2013 Aurélien Chabot <aurelien@chabot.fr>
 * <p>
 * This file is part of DroidUPNP.
 * <p>
 * DroidUPNP is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * DroidUPNP is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with DroidUPNP.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.m3sv.plainupnp.upnp;


import com.m3sv.plainupnp.data.upnp.DIDLContainer;
import com.m3sv.plainupnp.data.upnp.DIDLObject;

public class DIDLObjectDisplay {

    protected static final String TAG = "DIDLContentDisplay";

    private final DIDLObject didl;

    public DIDLObjectDisplay(DIDLObject didl) {
        this.didl = didl;
    }

    public DIDLObject getDIDLObject() {
        return didl;
    }

    public String getTitle() {
        return didl.getTitle();
    }

    public String getDescription() {
        return didl.getDescription();
    }

    public String getCount() {
        return didl.getCount();
    }

    public int getIcon() {
        return didl.getIcon();
    }

    @Override
    public String toString() {
        if (didl instanceof DIDLContainer)
            return didl.getTitle() + " (" + ((DIDLContainer) didl).getChildCount() + ")";

        return didl.getTitle();
    }
}
