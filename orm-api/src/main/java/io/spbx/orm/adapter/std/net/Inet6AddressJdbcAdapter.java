package io.spbx.orm.adapter.std.net;

import io.spbx.orm.adapter.JdbcAdapt;

import java.net.Inet6Address;

@JdbcAdapt(Inet6Address.class)
public class Inet6AddressJdbcAdapter extends BaseInetAddressJdbcAdapter<Inet6Address> {
    public static final Inet6AddressJdbcAdapter ADAPTER = new Inet6AddressJdbcAdapter();
}
