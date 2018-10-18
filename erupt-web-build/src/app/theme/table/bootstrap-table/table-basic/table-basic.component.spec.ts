import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { TableBasicComponent } from './table-basic.component';

describe('TableBasicComponent', () => {
  let component: TableBasicComponent;
  let fixture: ComponentFixture<TableBasicComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ TableBasicComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TableBasicComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
