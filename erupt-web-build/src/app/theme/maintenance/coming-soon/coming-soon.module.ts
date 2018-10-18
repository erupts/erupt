import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ComingSoonComponent } from './coming-soon.component';
import {ComingSoonRoutingModule} from './coming-soon-routing.module';
import {SharedModule} from '../../../shared/shared.module';

@NgModule({
  imports: [
    CommonModule,
    ComingSoonRoutingModule,
    SharedModule
  ],
  declarations: [ComingSoonComponent]
})
export class ComingSoonModule { }
