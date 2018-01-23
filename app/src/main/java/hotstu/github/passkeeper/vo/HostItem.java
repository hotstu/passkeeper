package hotstu.github.passkeeper.vo;

import hotstu.github.passkeeper.db.HostEntity;
import hotstu.github.passkeeper.tree.Parent;

/**
 * @author hglf
 * @since 2018/1/23
 */
public class HostItem extends Parent implements Item<HostEntity> {
    private HostEntity data;

    public HostItem() {
    }

    public HostItem(HostEntity data) {
        this.data = data;
    }

    @Override
    public void setData(HostEntity data) {
        this.data = data;
    }

    @Override
    public HostEntity getData() {
        return data;
    }

    @Override
    public String getText() {
        return data.hostname;
    }
}
