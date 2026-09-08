package com.dmfs.operations.entity;
import com.dmfs.company.entity.SubscriberCompany;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
@Entity @Table(name="management_notification")
public class ManagementNotification {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="company_id",nullable=false) private SubscriberCompany company;
 @Column(name="notification_type",nullable=false) private String notificationType;
 @Column(nullable=false,columnDefinition="TEXT") private String message;
 @Column(name="created_at") private OffsetDateTime createdAt;
 public void setCompany(SubscriberCompany v){company=v;} public void setNotificationType(String v){notificationType=v;} public void setMessage(String v){message=v;} public void setCreatedAt(OffsetDateTime v){createdAt=v;}
}
