package com.nilhcem.bblfr.model;

import ollie.Model;
import ollie.annotation.Column;
import ollie.annotation.Table;

@Table("baggers_tags")
public class BaggerTag extends Model {

    @Column("bagger_id")
    public Bagger mBagger;

    @Column("tag_id")
    public Tag mTag;

    public BaggerTag(Bagger bagger, Tag tag) {
        mBagger = bagger;
        mTag = tag;
    }
}