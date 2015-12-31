/*
 * Copyright 2010-2013 Ning, Inc.
 *
 * Ning licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package org.killbill.billing.subscription.engine.dao.model;

import java.util.UUID;

import org.joda.time.DateTime;

import org.killbill.billing.subscription.events.SubscriptionBaseEvent;
import org.killbill.billing.subscription.events.SubscriptionBaseEvent.EventType;
import org.killbill.billing.subscription.events.EventBaseBuilder;
import org.killbill.billing.subscription.events.phase.PhaseEvent;
import org.killbill.billing.subscription.events.phase.PhaseEventBuilder;
import org.killbill.billing.subscription.events.phase.PhaseEventData;
import org.killbill.billing.subscription.events.user.ApiEvent;
import org.killbill.billing.subscription.events.user.ApiEventBuilder;
import org.killbill.billing.subscription.events.user.ApiEventCancel;
import org.killbill.billing.subscription.events.user.ApiEventChange;
import org.killbill.billing.subscription.events.user.ApiEventCreate;
import org.killbill.billing.subscription.events.user.ApiEventMigrateBilling;
import org.killbill.billing.subscription.events.user.ApiEventMigrateSubscription;
import org.killbill.billing.subscription.events.user.ApiEventReCreate;
import org.killbill.billing.subscription.events.user.ApiEventTransfer;
import org.killbill.billing.subscription.events.user.ApiEventType;
import org.killbill.billing.subscription.events.user.ApiEventUncancel;
import org.killbill.billing.subscription.exceptions.SubscriptionBaseError;
import org.killbill.billing.util.dao.TableName;
import org.killbill.billing.util.entity.dao.EntityModelDao;
import org.killbill.billing.util.entity.dao.EntityModelDaoBase;

public class SubscriptionEventModelDao extends EntityModelDaoBase implements EntityModelDao<SubscriptionBaseEvent> {

    private long totalOrdering;
    private EventType eventType;
    private ApiEventType userType;
    private DateTime requestedDate; // deprecated (similar to effectiveDate)
    private DateTime effectiveDate;
    private UUID subscriptionId;
    private String planName;
    private String phaseName;
    private String priceListName;
    private long currentVersion;
    private boolean isActive;

    public SubscriptionEventModelDao() {
    /* For the DAO mapper */
    }

    public SubscriptionEventModelDao(final UUID id, final long totalOrdering, final EventType eventType, final ApiEventType userType,
                                     final DateTime requestedDate, final DateTime effectiveDate, final UUID subscriptionId,
                                     final String planName, final String phaseName, final String priceListName, final long currentVersion,
                                     final boolean active, final DateTime createDate, final DateTime updateDate) {
        super(id, createDate, updateDate);
        this.totalOrdering = totalOrdering;
        this.eventType = eventType;
        this.userType = userType;
        this.requestedDate = requestedDate;
        this.effectiveDate = effectiveDate;
        this.subscriptionId = subscriptionId;
        this.planName = planName;
        this.phaseName = phaseName;
        this.priceListName = priceListName;
        this.currentVersion = currentVersion;
        this.isActive = active;
    }

    public SubscriptionEventModelDao(final SubscriptionBaseEvent src) {
        super(src.getId(), src.getCreatedDate(), src.getUpdatedDate());
        this.totalOrdering = src.getTotalOrdering();
        this.eventType = src.getType();
        this.userType = eventType == EventType.API_USER ? ((ApiEvent) src).getApiEventType() : null;
        this.requestedDate = src.getEffectiveDate();
        this.effectiveDate = src.getEffectiveDate();
        this.subscriptionId = src.getSubscriptionId();
        this.planName = eventType == EventType.API_USER ? ((ApiEvent) src).getEventPlan() : null;
        this.phaseName = eventType == EventType.API_USER ? ((ApiEvent) src).getEventPlanPhase() : ((PhaseEvent) src).getPhase();
        this.priceListName = eventType == EventType.API_USER ? ((ApiEvent) src).getPriceList() : null;
        this.currentVersion = src.getActiveVersion();
        this.isActive = src.isActive();
    }

    public long getTotalOrdering() {
        return totalOrdering;
    }

    public EventType getEventType() {
        return eventType;
    }

    public ApiEventType getUserType() {
        return userType;
    }

    public DateTime getRequestedDate() {
        return requestedDate;
    }

    public DateTime getEffectiveDate() {
        return effectiveDate;
    }

    public UUID getSubscriptionId() {
        return subscriptionId;
    }

    public String getPlanName() {
        return planName;
    }

    public String getPhaseName() {
        return phaseName;
    }

    public String getPriceListName() {
        return priceListName;
    }

    public long getCurrentVersion() {
        return currentVersion;
    }

    // TODO required for jdbi binder
    public boolean getIsActive() {
        return isActive;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setTotalOrdering(final long totalOrdering) {
        this.totalOrdering = totalOrdering;
    }

    public void setEventType(final EventType eventType) {
        this.eventType = eventType;
    }

    public void setUserType(final ApiEventType userType) {
        this.userType = userType;
    }

    public void setRequestedDate(final DateTime requestedDate) {
        this.requestedDate = requestedDate;
    }

    public void setEffectiveDate(final DateTime effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public void setSubscriptionId(final UUID subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public void setPlanName(final String planName) {
        this.planName = planName;
    }

    public void setPhaseName(final String phaseName) {
        this.phaseName = phaseName;
    }

    public void setPriceListName(final String priceListName) {
        this.priceListName = priceListName;
    }

    public void setCurrentVersion(final long currentVersion) {
        this.currentVersion = currentVersion;
    }

    public void setIsActive(final boolean isActive) {
        this.isActive = isActive;
    }

    public static SubscriptionBaseEvent toSubscriptionEvent(final SubscriptionEventModelDao src) {

        if (src == null) {
            return null;
        }

        final EventBaseBuilder<?> base = ((src.getEventType() == EventType.PHASE) ?
                                          new PhaseEventBuilder() :
                                          new ApiEventBuilder())
                .setTotalOrdering(src.getTotalOrdering())
                .setUuid(src.getId())
                .setSubscriptionId(src.getSubscriptionId())
                .setCreatedDate(src.getCreatedDate())
                .setUpdatedDate(src.getUpdatedDate())
                .setEffectiveDate(src.getEffectiveDate())
                .setActiveVersion(src.getCurrentVersion())
                .setActive(src.isActive());

        SubscriptionBaseEvent result;
        if (src.getEventType() == EventType.PHASE) {
            result = (new PhaseEventBuilder(base).setPhaseName(src.getPhaseName())).build();
        } else if (src.getEventType() == EventType.API_USER) {
            final ApiEventBuilder builder = new ApiEventBuilder(base)
                    .setEventPlan(src.getPlanName())
                    .setEventPlanPhase(src.getPhaseName())
                    .setEventPriceList(src.getPriceListName())
                    .setApiEventType(src.getUserType())
                    .setApiEventType(src.getUserType())
                    .setFromDisk(true);
            result = builder.build();
        } else {
            throw new SubscriptionBaseError(String.format("Can't figure out event %s", src.getEventType()));
        }
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("SubscriptionEventModelDao");
        sb.append("{totalOrdering=").append(totalOrdering);
        sb.append(", eventType=").append(eventType);
        sb.append(", userType=").append(userType);
        sb.append(", requestedDate=").append(requestedDate);
        sb.append(", effectiveDate=").append(effectiveDate);
        sb.append(", subscriptionId=").append(subscriptionId);
        sb.append(", planName='").append(planName).append('\'');
        sb.append(", phaseName='").append(phaseName).append('\'');
        sb.append(", priceListName='").append(priceListName).append('\'');
        sb.append(", currentVersion=").append(currentVersion);
        sb.append(", isActive=").append(isActive);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        final SubscriptionEventModelDao that = (SubscriptionEventModelDao) o;

        if (currentVersion != that.currentVersion) {
            return false;
        }
        if (isActive != that.isActive) {
            return false;
        }
        if (totalOrdering != that.totalOrdering) {
            return false;
        }
        if (effectiveDate != null ? !effectiveDate.equals(that.effectiveDate) : that.effectiveDate != null) {
            return false;
        }
        if (eventType != that.eventType) {
            return false;
        }
        if (phaseName != null ? !phaseName.equals(that.phaseName) : that.phaseName != null) {
            return false;
        }
        if (planName != null ? !planName.equals(that.planName) : that.planName != null) {
            return false;
        }
        if (priceListName != null ? !priceListName.equals(that.priceListName) : that.priceListName != null) {
            return false;
        }
        if (requestedDate != null ? !requestedDate.equals(that.requestedDate) : that.requestedDate != null) {
            return false;
        }
        if (subscriptionId != null ? !subscriptionId.equals(that.subscriptionId) : that.subscriptionId != null) {
            return false;
        }
        if (userType != that.userType) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (int) (totalOrdering ^ (totalOrdering >>> 32));
        result = 31 * result + (eventType != null ? eventType.hashCode() : 0);
        result = 31 * result + (userType != null ? userType.hashCode() : 0);
        result = 31 * result + (requestedDate != null ? requestedDate.hashCode() : 0);
        result = 31 * result + (effectiveDate != null ? effectiveDate.hashCode() : 0);
        result = 31 * result + (subscriptionId != null ? subscriptionId.hashCode() : 0);
        result = 31 * result + (planName != null ? planName.hashCode() : 0);
        result = 31 * result + (phaseName != null ? phaseName.hashCode() : 0);
        result = 31 * result + (priceListName != null ? priceListName.hashCode() : 0);
        result = 31 * result + (int) (currentVersion ^ (currentVersion >>> 32));
        result = 31 * result + (isActive ? 1 : 0);
        return result;
    }

    @Override
    public TableName getTableName() {
        return TableName.SUBSCRIPTION_EVENTS;
    }

    @Override
    public TableName getHistoryTableName() {
        return null;
    }
}
