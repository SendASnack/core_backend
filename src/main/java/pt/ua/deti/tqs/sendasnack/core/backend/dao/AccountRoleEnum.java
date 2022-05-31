package pt.ua.deti.tqs.sendasnack.core.backend.dao;

import org.springframework.security.core.GrantedAuthority;

public enum AccountRoleEnum implements GrantedAuthority {
    RIDER, BUSINESS;

    @Override
    public String getAuthority() {
        return this.toString();
    }

}
