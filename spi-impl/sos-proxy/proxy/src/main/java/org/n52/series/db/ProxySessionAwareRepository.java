/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.series.db;

import org.n52.io.response.ServiceOutput;
import org.n52.series.db.beans.ServiceEntity;

/**
 *
 * @author jansch
 */
public class ProxySessionAwareRepository {

    protected ServiceOutput createCondensedService(ServiceEntity entity) {
        ServiceOutput result = new ServiceOutput();
        result.setId(Long.toString(entity.getPkid()));
        result.setLabel(entity.getServiceId());
        return result;
    }

}
