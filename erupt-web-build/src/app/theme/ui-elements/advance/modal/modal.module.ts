import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ModalComponent } from './modal.component';
import {ModalRoutingModule} from './modal-routing.module';
import {SharedModule} from '../../../../shared/shared.module';

@NgModule({
  imports: [
    CommonModule,
    ModalRoutingModule,
    SharedModule
  ],
  declarations: [ModalComponent]
})
export class ModalModule { }
