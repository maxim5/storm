package io.spbx.orm.adapter.std.net;

import io.spbx.orm.adapter.JdbcAdapt;
import io.spbx.util.base.annotate.Stateless;

import java.net.Inet6Address;

@Stateless
@JdbcAdapt(Inet6Address.class)
public class Inet6AddressJdbcAdapter extends BaseInetAddressJdbcAdapter<Inet6Address> {
    public static final Inet6AddressJdbcAdapter ADAPTER = new Inet6AddressJdbcAdapter();
}
