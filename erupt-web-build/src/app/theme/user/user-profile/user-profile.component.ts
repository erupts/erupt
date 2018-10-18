import {Component, OnInit} from '@angular/core';
import {animate, style, transition, trigger} from '@angular/animations';

@Component({
  selector: 'app-user-profile',
  templateUrl: './user-profile.component.html',
  styleUrls: [
    './user-profile.component.scss',
    '../../../../assets/icon/icofont/css/icofont.scss'
  ],
  animations: [
    trigger('fadeInOutTranslate', [
      transition(':enter', [
        style({opacity: 0}),
        animate('400ms ease-in-out', style({opacity: 1}))
      ]),
      transition(':leave', [
        style({transform: 'translate(0)'}),
        animate('400ms ease-in-out', style({opacity: 0}))
      ])
    ])
  ]
})
export class UserProfileComponent implements OnInit {
  editProfile = true;
  editProfileIcon = 'icofont-edit';

  editAbout = true;
  editAboutIcon = 'icofont-edit';

  public editor;
  public editorContent: string;

  public data: any;
  public rowsOnPage = 10;
  public filterQuery = '';
  public sortBy = '';
  public sortOrder = 'desc';
  profitChartOption: any;

  rowsContact = [];
  loadingIndicator = true;
  reorderable = true;

  constructor() {
    this.fetchContactData((data) => {
      this.rowsContact = data;
      setTimeout(() => { this.loadingIndicator = false; }, 1500);
    });
  }

  ngOnInit() {

    setTimeout(() => {
      this.editorContent = 'But I must explain to you how all this mistaken idea of denouncing pleasure and praising ';
      this.editorContent += 'pain was born and I will give you a complete account of the system, and expound the actual ';
      this.editorContent += 'teachings of the great explorer of the truth, the master-builder of human happiness. No one ';
      this.editorContent += 'rejects, dislikes, or avoids pleasure itself, because it is pleasure, but because those who ';
      this.editorContent += 'do not know how to pursue pleasure rationally encounter consequences that are extremely ';
      this.editorContent += 'painful. Nor again is there anyone who loves or pursues or desires to obtain pain of itself, ';
      this.editorContent += 'because it is pain, but because occasionally circumstances occur in which toil and pain can ';
      this.editorContent += 'procure him some great pleasure. To take a trivial example, which of us ever undertakes ';
      this.editorContent += 'laborious physical exercise, except to obtain some advantage from it? But who has any right ';
      this.editorContent += 'to find fault with a man who chooses to enjoy a pleasure that has no annoying consequences, ';
      this.editorContent += 'or one who avoids a pain that produces no resultant pleasure? On the other hand, we denounce ';
      this.editorContent += 'with righteous indignation and dislike men who are so beguiled and demoralized by the charms ';
      this.editorContent += 'of pleasure of the moment, so blinded by desire, that they cannot foresee the pain and ';
      this.editorContent += 'trouble that are bound to ensue; and equal blame belongs to those who fail in their duty ';
      this.editorContent += 'through weakness of will, which is the same as saying through shrinking from toil and pain. ';
      this.editorContent += 'These cases are perfectly simple and easy to distinguish. In a free hour, when our power of ';
      this.editorContent += 'choice is untrammelled and when nothing prevents our being able To Do what we like best, ';
      this.editorContent += 'every pleasure is to be welcomed and every pain avoided. But in certain circumstances and ';
      this.editorContent += 'owing to the claims of duty or the obligations of business it will frequently occur that ';
      this.editorContent += 'pleasures have to be repudiated and annoyances accepted. The wise man therefore always holds';
      this.editorContent += 'in these matters to this principle of selection: he rejects pleasures to secure other ';
      this.editorContent += 'greater pleasures, or else he endures pains to avoid worse pain.';
    }, 2800);

    setTimeout(() => {
      this.profitChartOption = {
        tooltip: {
          trigger: 'item',
          formatter: function(params) {
            const date = new Date(params.value[0]);
            let data = date.getFullYear() + '-' + (date.getMonth() + 1) + '-' + date.getDate() + ' ';
            data += date.getHours() + ':' + date.getMinutes();
            return data + '<br/>' + params.value[1] + ', ' + params.value[2];
          },
          responsive: true
        },
        dataZoom: {
          show: true,
          start: 70
        },
        legend: {
          data: ['Profit']
        },
        grid: {
          y2: 80
        },
        xAxis: [{
          type: 'time',
          splitNumber: 10
        }],
        yAxis: [{
          type: 'value'
        }],
        series: [{
          name: 'Profit',
          type: 'line',
          showAllSymbol: true,
          symbolSize: function(value) {
            return Math.round(value[2] / 10) + 2;
          },
          data: (function() {
            const d: any = [];
            let len = 0;
            const now = new Date();
            while (len++ < 200) {
              const random1: any = (Math.random() * 30).toFixed(2);
              const random2: any = (Math.random() * 100).toFixed(2);
              d.push([ new Date(2014, 9, 1, 0, len * 10000), random1 - 0, random2 - 0 ]);
            }
            return d;
          })()
        }]
      };
    }, 1);
  }

  fetchContactData(cb) {
    const req = new XMLHttpRequest();
    req.open('GET', 'assets/data/data.json');

    req.onload = () => {
      cb(JSON.parse(req.response));
    };

    req.send();
  }

  toggleEditProfile() {
    this.editProfileIcon = (this.editProfileIcon === 'icofont-close') ? 'icofont-edit' : 'icofont-close';
    this.editProfile = !this.editProfile;
  }

  toggleEditAbout() {
    this.editAboutIcon = (this.editAboutIcon === 'icofont-close') ? 'icofont-edit' : 'icofont-close';
    this.editAbout = !this.editAbout;
  }

}
