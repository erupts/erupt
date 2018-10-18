import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NotificationComponent } from './notification.component';
import {NotificationRoutingModule} from './notification-routing.module';
import {SharedModule} from '../../../../shared/shared.module';
import {ToastyModule} from 'ng2-toasty';

@NgModule({
  imports: [
    CommonModule,
    NotificationRoutingModule,
    SharedModule,
    ToastyModule.forRoot()
  ],
  declarations: [NotificationComponent]
})
export class NotificationModule { }
