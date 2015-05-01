/*
 *  The MIT License
 *
 *  Copyright 2015 Ericsson All rights reserved.
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */

package com.ericsson.jenkinsci.hajp.extensions;

import hudson.ExtensionPoint;

/**
 * A abstract class for listening to HAJP Cluster membership status events.
 */
public abstract class HajpClusterExtension implements ExtensionPoint {

    /**
     * Method that will be called when the active master is selected.
     *
     * @param isSelected is selected for being or not the active master .
     */
    public abstract void notifyOnActiveMasterSelection(boolean isSelected);

    /**
     * Method that will be called when you join cluster
     */
    public abstract void notifyOnClusterJoined();

    /**
     * Method that will be called when you disconnect from cluster
     */
    public abstract void notifyOnClusterDisconnected();

}
