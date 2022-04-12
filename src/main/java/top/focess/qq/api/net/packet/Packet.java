package top.focess.qq.api.net.packet;

import top.focess.util.serialize.FocessSerializable;

/**
 * This is the base class of all packets.
 */
public abstract class Packet implements FocessSerializable {

    /**
     * Get the packet id
     *
     * @return the packet id
     */
    public abstract int getId();
}
