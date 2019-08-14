package org.foo.app;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.netty.NettyChannelBuilder;
import io.grpc.internal.DnsNameResolverProvider;
import java.util.*;
import java.util.concurrent.TimeUnit;
import io.opencensus.tags.Tags;
import com.grpc.gobgp.GobgpApiGrpc;
import com.grpc.gobgp.GobgpOuter;
import com.grpc.gobgp.AttrOuter;
import com.google.protobuf.Any;

public class GobgpClient {
    //private final NettyChannelBuilder channel;
    private final ManagedChannel ch = NettyChannelBuilder.forAddress("localhost", 50051)
    .nameResolverFactory(new DnsNameResolverProvider())
    .usePlaintext(true)
    .build();
    private final GobgpApiGrpc.GobgpApiBlockingStub blockingStub = GobgpApiGrpc.newBlockingStub(ch);
    //  public GobgpClient(String host, int port){
    //      this(NettyChannelBuilder.forAddress(host, port));
    //  }
    // private GobgpClient(ManagedChannelBuilder<?> channelBuilder){
    //     channel = channelBuilder.build();
    //     blockingStub = GobgpApiGrpc.newBlockingStub(channel);
    // }
    // public void shutdown()throws InterruptedException{
    //     channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    // }

    public void shutdown()throws InterruptedException{
        ch.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }
    
    public String setup(String name){

        AttrOuter.IPAddressPrefix nlri = AttrOuter.IPAddressPrefix.newBuilder().setPrefix("10.0.0.0").setPrefixLen(24).build();
        AttrOuter.OriginAttribute origin = AttrOuter.OriginAttribute.newBuilder().setOrigin(2).build();
        AttrOuter.AsSegment segment = AttrOuter.AsSegment.newBuilder().addNumbers(100).addNumbers(200).setType(2).build();
        AttrOuter.AsPathAttribute pathattr = AttrOuter.AsPathAttribute.newBuilder().addSegments(segment).build();
        AttrOuter.NextHopAttribute nexthop = AttrOuter.NextHopAttribute.newBuilder().setNextHop("1.1.1.1").build();
        GobgpOuter.Family family = GobgpOuter.Family.newBuilder().setAfi(GobgpOuter.Family.Afi.AFI_IP).setSafi(GobgpOuter.Family.Safi.SAFI_UNICAST).build();
        //Any[] attributes = {Any.pack(origin),Any.pack(pathattr),Any.pack(nexthop)};
        //Map<String, Any> reMap = new HashMap<String, Any>();
        //reMap.put("nlri", Any.pack(nlri));
        //reMap.put("pattrs", attributes);
        //reMap.put("family", Any.pack(attributes));
        //reMap.put("pattrs", Any.pack(listValue));
        GobgpOuter.Path path = GobgpOuter.Path.newBuilder().setNlri(Any.pack(nlri)).setFamily(family).addPattrs(Any.pack(origin)).addPattrs(Any.pack(pathattr)).addPattrs(Any.pack(nexthop)).build();
        GobgpOuter.AddPathRequest req = 
                 GobgpOuter.AddPathRequest.newBuilder()
                 .setTableType(GobgpOuter.TableType.GLOBAL)
                 .setPath(path)
                 .build();
        GobgpOuter.AddPathResponse response = blockingStub.addPath(req);

        AttrOuter.OriginAttribute evpnorigin = AttrOuter.OriginAttribute.newBuilder().setOrigin(2).build();
        AttrOuter.RouteDistinguisherIPAddress rd = AttrOuter.RouteDistinguisherIPAddress.newBuilder().setAdmin("1.1.1.1").setAssigned(100).build();
        AttrOuter.EthernetSegmentIdentifier esi = AttrOuter.EthernetSegmentIdentifier.newBuilder().setType(0).build();
        AttrOuter.EVPNEthernetAutoDiscoveryRoute evpnnlri = AttrOuter.EVPNEthernetAutoDiscoveryRoute.newBuilder().setEsi(esi).setRd(Any.pack(rd)).setEthernetTag(100).build();
        AttrOuter.NextHopAttribute evpnnexthop = AttrOuter.NextHopAttribute.newBuilder().setNextHop("0.0.0.0").build();
        GobgpOuter.Family evpnfamily = GobgpOuter.Family.newBuilder().setAfi(GobgpOuter.Family.Afi.AFI_L2VPN).setSafi(GobgpOuter.Family.Safi.SAFI_EVPN).build();
        GobgpOuter.Path evpnpath = GobgpOuter.Path.newBuilder().setNlri(Any.pack(evpnnlri)).setFamily(evpnfamily).addPattrs(Any.pack(evpnnexthop)).addPattrs(Any.pack(evpnorigin)).build();
        req = GobgpOuter.AddPathRequest.newBuilder()
                 .setTableType(GobgpOuter.TableType.GLOBAL)
                 .setPath(evpnpath)
                 .build();
        response = blockingStub.addPath(req);
        return response.getUuid().toString();
    }
}