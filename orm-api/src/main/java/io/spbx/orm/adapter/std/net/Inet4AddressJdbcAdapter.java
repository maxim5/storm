package io.spbx.orm.adapter.std.net;

import io.spbx.orm.adapter.JdbcAdapt;

import java.net.Inet4Address;

@JdbcAdapt(Inet4Address.class)
public class Inet4AddressJdbcAdapter extends BaseInetAddressJdbcAdapter<Inet4Address> {
    public static final Inet4AddressJdbcAdapter ADAPTER = new Inet4AddressJdbcAdapter();
}
