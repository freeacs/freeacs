/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.net.io;

import java.util.EventObject;

/**
 * A CopyStreamEvent is triggered after every write performed by a
 * stream copying operation.  The event stores the number of bytes
 * transferred by the write triggering the event as well as the total
 * number of bytes transferred so far by the copy operation.
 * <p>
 * <p>
 * @see CopyStreamListener
 * @see CopyStreamAdapter
 * @see Util
 * @author <a href="mailto:savarese@apache.org">Daniel F. Savarese</a>
 * @version $Id: CopyStreamEvent.java 489397 2006-12-21 16:28:51Z rwinston $
 */
@SuppressWarnings("serial")
public class CopyStreamEvent extends EventObject
{
    /**
     * Constant used to indicate the stream size is unknown.
     */
    public static final long UNKNOWN_STREAM_SIZE = -1;

    private int bytesTransferred;
    private long totalBytesTransferred;
    private long streamSize;

    /**
     * Creates a new CopyStreamEvent instance.
     * @param source  The source of the event.
     * @param totalBytesTransferred The total number of bytes transferred so
     *   far during a copy operation.
     * @param bytesTransferred  The number of bytes transferred during the
     *        write that triggered the CopyStreamEvent.
     * @param streamSize  The number of bytes in the stream being copied.
     *          This may be set to <code>UNKNOWN_STREAM_SIZE</code> if the
     *          size is unknown.
     */
    public CopyStreamEvent(Object source, long totalBytesTransferred,
                           int bytesTransferred, long streamSize)
    {
        super(source);
        this.bytesTransferred = bytesTransferred;
        this.totalBytesTransferred = totalBytesTransferred;
        this.streamSize = streamSize;
    }

    /**
     * Returns the number of bytes transferred by the write that triggered
     * the event.
     * @return The number of bytes transferred by the write that triggered
     * the vent.
     */
    public int getBytesTransferred()
    {
        return bytesTransferred;
    }

    /**
     * Returns the total number of bytes transferred so far by the copy
     * operation.
     * @return The total number of bytes transferred so far by the copy
     * operation.
     */
    public long getTotalBytesTransferred()
    {
        return totalBytesTransferred;
    }

    /**
     * Returns the size of the stream being copied.
     * This may be set to <code>UNKNOWN_STREAM_SIZE</code> if the
     * size is unknown.
     * @return The size of the stream being copied.
     */
    public long getStreamSize()
    {
        return streamSize;
    }
}
