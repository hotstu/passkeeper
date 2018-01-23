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
    }