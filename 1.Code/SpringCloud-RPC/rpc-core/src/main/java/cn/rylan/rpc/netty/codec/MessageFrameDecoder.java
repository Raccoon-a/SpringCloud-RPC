package cn.rylan.rpc.netty.codec;


import cn.rylan.rpc.netty.constant.ProtocolConstants;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.springframework.stereotype.Component;

/**
 * author: Rylan
 * create: 2022-10-25 16:19
 * desc: 解析消息帧
 * links: https://www.cnblogs.com/java-chen-hao/p/11571229.html#_label0
 **/

@Component
public class MessageFrameDecoder extends LengthFieldBasedFrameDecoder {

    /**
     * maxFrameLength：最大帧长度。也就是可以接收的数据的最大长度。如果超过，此次数据会被丢弃。<p>
     * lengthFieldOffset：长度域偏移。就是说数据开始的几个字节可能不是表示数据长度，需要后移几个字节才是长度域。<p>
     * lengthFieldLength：长度域字节数。用几个字节来表示数据长度。<p>
     * lengthAdjustment：数据长度修正。因为长度域指定的长度可以使header+body的整个长度，也可以只是body的长度。如果表示header+body的整个长度，那么我们需要修正数据长度。<p>
     * initialBytesToStrip：跳过的字节数。如果你需要接收header+body的所有数据，此值就是0，如果你只想接收body数据，那么需要跳过header所占用的字节数。<p>
     */
    public MessageFrameDecoder(){
        this(ProtocolConstants.MAX_FRAME,6,4,-10,0);
    }

    public MessageFrameDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength, int lengthAdjustment, int initialBytesToStrip) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip);
    }
}
