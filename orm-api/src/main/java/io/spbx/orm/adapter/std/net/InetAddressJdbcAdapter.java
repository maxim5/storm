package io.spbx.orm.adapter.std.net;

import io.spbx.orm.adapter.JdbcAdapt;
import io.spbx.util.base.annotate.Stateless;

import java.net.InetAddress;

@Stateless
@JdbcAdapt(InetAddress.class)
public class InetAddressJdbcAdapter extends BaseInetAddressJdbcAdapter<InetAddress> {
    public static final InetAddressJdbcAdapter ADAPTER = new InetAddressJdbcAdapter();
}
