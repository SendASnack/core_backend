package pt.ua.deti.tqs.sendasnack.core.backend.dao;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Generated;
import pt.ua.deti.tqs.sendasnack.core.backend.model.webhooks.WebHook;
import pt.ua.deti.tqs.sendasnack.core.backend.utils.WebHookEvent;

@Generated
@Data
@AllArgsConstructor
public class WebHookDAO implements IEntityDAO<WebHook> {

    private Long id;
    private String businessUsername;
    private HookDAO hook;
    private WebHookEvent when;

    @Override
    public WebHook toDataEntity() {
        return new WebHook(id, businessUsername, hook.toDataEntity(), when);
    }

}
