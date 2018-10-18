import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { TableBasicDataComponent } from './table-basic-data.component';

describe('TableBasicDataComponent', () => {
  let component: TableBasicDataComponent;
  let fixture: ComponentFixture<TableBasicDataComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ TableBasicDataComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TableBasicDataComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
