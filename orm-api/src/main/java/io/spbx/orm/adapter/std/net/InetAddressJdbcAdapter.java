package io.spbx.orm.adapter.std.net;

import io.spbx.orm.adapter.JdbcAdapt;

import java.net.InetAddress;

@JdbcAdapt(InetAddress.class)
public class InetAddressJdbcAdapter extends BaseInetAddressJdbcAdapter<InetAddress> {
    public static final InetAddressJdbcAdapter ADAPTER = new InetAddressJdbcAdapter();
}
