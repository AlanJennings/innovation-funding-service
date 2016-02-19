package com.worth.ifs.user.mapper;

import com.worth.ifs.application.mapper.ApplicationMapper;
import com.worth.ifs.commons.mapper.BaseMapper;
import com.worth.ifs.commons.mapper.GlobalMapperConfig;
import com.worth.ifs.user.domain.ProcessRole;
import com.worth.ifs.user.resource.ProcessRoleResource;
import org.mapstruct.Mapper;

@Mapper(
    config = GlobalMapperConfig.class,
    uses = {
        ApplicationMapper.class,
        RoleMapper.class,
        OrganisationMapper.class
    }
)
public abstract class ProcessRoleMapper  extends BaseMapper<ProcessRole, ProcessRoleResource, Long> {

    public Long mapProcessRoleToId(ProcessRole object) {
        if (object == null) {
            return null;
        }
        return object.getId();
    }
}