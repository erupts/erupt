import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {AlertComponent, RemoveAlertDirective} from './alert.component';
import {AlertRoutingModule} from './alert-routing.module';
import {SharedModule} from '../../../../shared/shared.module';

@NgModule({
  imports: [
    CommonModule,
    AlertRoutingModule,
    SharedModule
  ],
  declarations: [
    AlertComponent,
    RemoveAlertDirective
  ]
})
export class AlertModule { }
