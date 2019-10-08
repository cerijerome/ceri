package ceri.ent.mongo;

import java.util.List;
import ceri.common.net.HostPort;
import ceri.common.util.EqualsUtil;
import ceri.common.util.HashCoder;

public class HostGroup {
	public final String name;
	public final HostList hosts;

	public static HostGroup of(String name, HostPort... hosts) {
		return of(name, HostList.of(hosts));
	}

	public static HostGroup of(String name, Iterable<HostPort> hosts) {
		return of(name, HostList.of(hosts));
	}

	public static HostGroup of(String name, HostList hosts) {
		return new HostGroup(name, hosts);
	}

	HostGroup(String name, HostList hosts) {
		this.name = name;
		this.hosts = hosts;
	}

	public List<HostPort> hosts() {
		return hosts.hosts;
	}

	@Override
	public int hashCode() {
		return HashCoder.hash(name, hosts);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof HostGroup)) return false;
		HostGroup other = (HostGroup) obj;
		if (!EqualsUtil.equals(name, other.name)) return false;
		if (!EqualsUtil.equals(hosts, other.hosts)) return false;
		return true;
	}

	@Override
	public String toString() {
		return name + "/" + hosts;
	}

}
