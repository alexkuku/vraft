package com.vraft.core.raft.handler;

import com.vraft.facade.raft.elect.RaftVoteResp;
import com.vraft.facade.raft.node.RaftNode;
import com.vraft.facade.raft.node.RaftNodeCtx;
import com.vraft.facade.raft.node.RaftNodeMgr;
import com.vraft.facade.rpc.RpcProcessor;
import com.vraft.facade.serializer.Serializer;
import com.vraft.facade.serializer.SerializerEnum;
import com.vraft.facade.serializer.SerializerMgr;
import com.vraft.facade.system.SystemCtx;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author jweihsz
 * @version 2024/3/14 20:43
 **/
public class RaftVoteRespHandler implements RpcProcessor {

    private final static Logger logger = LogManager.getLogger(RaftVoteRespHandler.class);

    private final SystemCtx sysCtx;

    public RaftVoteRespHandler(SystemCtx sysCtx) {
        this.sysCtx = sysCtx;
    }

    @Override
    public void handle(long connectId, long groupId, long nodeId, long msgId,
        byte[] header, byte[] body, boolean hasNext) throws Exception {
        SerializerMgr szMgr = sysCtx.getSerializerMgr();
        Serializer sz = szMgr.get(SerializerEnum.KRYO_ID);
        RaftVoteResp resp = sz.deserialize(body, RaftVoteResp.class);
        processVoteResp(resp, groupId, nodeId, msgId);
    }

    @Override
    public String uid() {
        return RaftVoteResp.class.getName();
    }

    private void processVoteResp(RaftVoteResp resp,
        long groupId, long nodeId, long msgId) throws Exception {
        final RaftNodeMgr mgr = sysCtx.getRaftNodeMgr();
        RaftNode node = mgr.getNodeMate(groupId, nodeId);
        if (node == null) {return;}
        final RaftNodeCtx nodeCtx = node.getNodeCtx();
        if (resp.isPre()) {
            nodeCtx.getElectMgr().processPreVoteResp(resp);
        } else {
            nodeCtx.getElectMgr().processForVoteResp(resp);
        }
    }
}
