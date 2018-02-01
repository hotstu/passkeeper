package hotstu.github.passkeeper.vo;

import hotstu.github.passkeeper.db.UserEntity;
import hotstu.github.passkeeper.tree.Child;

public  class UserItem extends Child implements Item<UserEntity> {
        private UserEntity data;
        public UserItem() {
        }
        public UserItem(UserEntity data) {
            this.data = data;
        }
        @Override
        public void setData(UserEntity data) {
            this.data = data;
        }

        @Override
        public UserEntity getData() {
            return data;
        }

        @Override
        public String getText() {
            return data.username;
        }

    @Override
    public int getId() {
        return data.id;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof UserItem)) {
            return false;
        }
        return this.getId() == ((UserItem) obj).getId() && this.data.hostId == ((UserItem) obj).data.hostId;
    }

    @Override
    public int hashCode() {
        return this.getId()  * 31 + this.getData().hostId * 17;
    }
}