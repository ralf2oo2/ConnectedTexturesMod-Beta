package team.chisel.ctm.client.util;

import it.unimi.dsi.fastutil.Hash;

import java.util.Objects;

public class IdentityStrategy<K> implements Hash.Strategy<K> {

    @Override
    public int hashCode(K o) {
        return Objects.hashCode(o);
    }

    @Override
    public boolean equals(K a, K b) {
        return Objects.equals(a, b);
    }

}
