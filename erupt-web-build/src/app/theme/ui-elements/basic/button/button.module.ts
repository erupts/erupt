import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ButtonComponent } from './button.component';
import {SharedModule} from '../../../../shared/shared.module';
import {ButtonRoutingModule} from './button-routing.module';

@NgModule({
  imports: [
    CommonModule,
    ButtonRoutingModule,
    SharedModule
  ],
  declarations: [ButtonComponent]
})
export class ButtonModule { }
