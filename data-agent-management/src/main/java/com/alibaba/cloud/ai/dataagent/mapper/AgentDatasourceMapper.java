/*
 * Copyright 2024-2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.cloud.ai.dataagent.mapper;

import com.alibaba.cloud.ai.dataagent.entity.AgentDatasource;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface AgentDatasourceMapper {

	/** Query associated data sources by saad_agent ID (including data source details) */
	@Select("SELECT ad.*, d.name, d.type, d.host, d.port, d.database_name, "
			+ "d.connection_url, d.username, d.password, d.status, d.test_status, d.description "
			+ "FROM saad_agent_datasource ad " + "LEFT JOIN saad_datasource d ON ad.datasource_id = d.id "
			+ "WHERE ad.agent_id = #{agentId} " + "ORDER BY ad.create_time DESC")
	List<AgentDatasource> selectByAgentIdWithDatasource(@Param("agentId") Long agentId);

	/** Query associated data sources by saad_agent ID */
	@Select("SELECT * FROM saad_agent_datasource WHERE agent_id = #{agentId} ORDER BY create_time DESC")
	List<AgentDatasource> selectByAgentId(@Param("agentId") Long agentId);

	/** Query active saad_datasource ID by saad_agent ID */
	@Select("SELECT datasource_id FROM saad_agent_datasource WHERE agent_id = #{agentId} AND is_active = 1")
	Integer selectActiveDatasourceIdByAgentId(@Param("agentId") Long agentId);

	/** Query association by saad_agent ID and data source ID */
	@Select("SELECT * FROM saad_agent_datasource WHERE agent_id = #{agentId} AND datasource_id = #{datasourceId}")
	AgentDatasource selectByAgentIdAndDatasourceId(@Param("agentId") Long agentId,
			@Param("datasourceId") Integer datasourceId);

	/** Disable all data sources for an agent */
	@Update("UPDATE saad_agent_datasource SET is_active = 0 WHERE agent_id = #{agentId}")
	int disableAllByAgentId(@Param("agentId") Long agentId);

	/**
	 * Count the number of enabled data sources for an agent (excluding the specified data
	 * source)
	 */
	@Select("SELECT COUNT(*) FROM saad_agent_datasource WHERE agent_id = #{agentId} AND is_active = 1 AND datasource_id != #{excludeDatasourceId}")
	int countActiveByAgentIdExcluding(@Param("agentId") Long agentId,
			@Param("excludeDatasourceId") Integer excludeDatasourceId);

	@Delete("DELETE FROM saad_agent_datasource WHERE datasource_id = #{datasourceId}")
	int deleteAllByDatasourceId(@Param("datasourceId") Integer datasourceId);

	@Insert("INSERT INTO saad_agent_datasource (agent_id, datasource_id, is_active) VALUES (#{agentId}, #{datasourceId}, 1)")
	int createNewRelationEnabled(@Param("agentId") Long agentId, @Param("datasourceId") Integer datasourceId);

	@Update("UPDATE saad_agent_datasource SET is_active = #{isActive} WHERE agent_id = #{agentId} AND datasource_id = #{datasourceId}")
	int updateRelation(@Param("agentId") Long agentId, @Param("datasourceId") Integer datasourceId,
			@Param("isActive") Integer isActive);

	default int enableRelation(Long agentId, Integer datasourceId) {
		return updateRelation(agentId, datasourceId, 1);
	}

	@Delete("DELETE FROM saad_agent_datasource WHERE agent_id = #{agentId} AND datasource_id = #{datasourceId}")
	int removeRelation(@Param("agentId") Long agentId, @Param("datasourceId") Integer datasourceId);

}
