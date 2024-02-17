package com.vraft.core.rpc;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.vraft.facade.rpc.RpcCallback;
import com.vraft.facade.rpc.RpcConsts;
import com.vraft.facade.rpc.RpcProcessor;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;

/**
 * @author jweihsz
 * @version 2024/2/12 10:00
 **/
public abstract class RpcAbstract {
    private final Map<ByteBuf, RpcProcessor<?>> PROCESSOR = new ConcurrentHashMap<>();

    public void unregisterProcessor(String uid) {
        if (uid == null || uid.isEmpty()) {return;}
        PROCESSOR.remove(RpcCommon.convert(uid));
    }

    public RpcProcessor<?> getProcessor(Object uid) {
        if (uid instanceof ByteBuf) {
            return PROCESSOR.get((ByteBuf)uid);
        } else {
            return null;
        }
    }

    public void registerProcessor(String uid, RpcProcessor<?> processor) {
        if (uid == null || uid.isEmpty() || processor == null) {return;}
        final ByteBuf bf = RpcCommon.convert(uid);
        PROCESSOR.put(bf, processor);
    }

    public void invokeOneway(Object channel, ByteBuf bf) throws Exception {
        if (!(channel instanceof Channel)) {return;}
        final Channel ch = (Channel)channel;
        ch.writeAndFlush(bf);
    }

    public void invokeTwoWay(Object channel, ByteBuf bf, long timeout,
        RpcCallback cb) throws Exception {
        if (!(channel instanceof Channel)) {return;}
        final Channel ch = (Channel)channel;
        ch.writeAndFlush(bf);
    }

    public abstract long msgId();

    private byte buildType(int rq, int type) {
        return (byte)((type & 0xFC) | (rq & 0x03));
    }

    public ByteBuf buildPkg(int rq, int ty, String uid, byte[] header, byte[] body) {
        int totalLen = RpcCommon.RPC_MATE_SIZE;
        byte[] bodyBuf, uidBuf, headerBuf;
        if (body == null) {
            bodyBuf = RpcCommon.EMPTY_BUFFER;
        } else {
            bodyBuf = body;
        }
        totalLen += bodyBuf.length;
        if (header == null) {
            headerBuf = RpcCommon.EMPTY_BUFFER;
        } else {
            headerBuf = header;
        }
        totalLen += headerBuf.length;
        if (uid == null) {
            uidBuf = RpcCommon.EMPTY_BUFFER;
        } else {
            uidBuf = uid.getBytes();
        }
        totalLen += uidBuf.length;
        final byte flag = buildType(rq, ty);
        ByteBuf mate = Unpooled.buffer(32);
        mate.writeShort(RpcConsts.RPC_MAGIC);
        mate.writeInt(totalLen);
        mate.writeByte(RpcConsts.RPC_VERSION);
        mate.writeByte(flag);
        mate.writeLong(msgId());
        mate.writeInt(uidBuf.length);
        mate.writeInt(headerBuf.length);
        mate.writeInt(bodyBuf.length);
        return Unpooled.wrappedBuffer(mate.array(), uidBuf, headerBuf, bodyBuf);
    }

}
