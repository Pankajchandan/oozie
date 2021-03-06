/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.oozie.executor.jpa;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.oozie.CoordinatorActionBean;
import org.apache.oozie.ErrorCode;
import org.apache.oozie.client.CoordinatorAction;
import org.apache.oozie.util.DateUtils;

/**
 * Load coordinator actions in READY state for a coordinator job.
 */
public class CoordJobGetReadyActionsJPAExecutor implements JPAExecutor<List<CoordinatorActionBean>> {

    private String coordJobId = null;
    private String executionOrder = null;

    public CoordJobGetReadyActionsJPAExecutor(String coordJobId, String executionOrder) {
        Objects.requireNonNull(coordJobId, "coordJobId cannot be null");
        this.coordJobId = coordJobId;
        this.executionOrder = executionOrder;
    }

    @Override
    public String getName() {
        return "CoordJobGetReadyActionsJPAExecutor";
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<CoordinatorActionBean> execute(EntityManager em) throws JPAExecutorException {
        List<CoordinatorActionBean> actionBeans = null;
        try {
            Query q;
            // check if executionOrder is FIFO, LIFO, LAST_ONLY, or NONE
            if (executionOrder.equalsIgnoreCase("FIFO")) {
                q = em.createNamedQuery("GET_COORD_ACTIONS_FOR_JOB_FIFO");
            }
            else {      // LIFO, LAST_ONLY, or NONE
                q = em.createNamedQuery("GET_COORD_ACTIONS_FOR_JOB_LIFO");
            }
            q.setParameter("jobId", coordJobId);

            List<Object[]> objectArrList = q.getResultList();
            actionBeans = new ArrayList<CoordinatorActionBean>();
            for (Object[] arr : objectArrList) {
                CoordinatorActionBean caa = getBeanForCoordinatorActionFromArray(arr);
                actionBeans.add(caa);
            }
            return actionBeans;
        }
        catch (Exception e) {
            throw new JPAExecutorException(ErrorCode.E0603, e.getMessage(), e);
        }
    }

    private CoordinatorActionBean getBeanForCoordinatorActionFromArray(Object arr[]) {
        CoordinatorActionBean bean = new CoordinatorActionBean();
        if (arr[0] != null) {
            bean.setId((String) arr[0]);
        }
        if (arr[1] != null) {
            bean.setActionNumber((Integer) arr[1]);
        }
        if (arr[2] != null) {
            bean.setJobId((String) arr[2]);
        }
        if (arr[3] != null) {
            bean.setStatus(CoordinatorAction.Status.valueOf((String) arr[3]));
        }
        if (arr[4] != null) {
            bean.setPending((Integer) arr[4]);
        }
        if (arr[5] != null) {
            bean.setNominalTime(DateUtils.toDate((Timestamp) arr[5]));
        }
        if (arr[6] != null) {
            bean.setCreatedTime(DateUtils.toDate((Timestamp) arr[6]));
        }
        return bean;
    }

}
