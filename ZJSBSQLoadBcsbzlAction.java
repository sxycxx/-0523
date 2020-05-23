package com.poweruniverse.app.action.zjk;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import com.poweruniverse.app.entity.tech.BiaoZhunGFFBSS;
import com.poweruniverse.app.entity.tech.BiaoZhunGFFBSSBZRY;
import com.poweruniverse.app.entity.tech.BiaoZhunGFFBSSSB;
import com.poweruniverse.app.entity.tech.BiaoZhunGFLXBZRY;
import com.poweruniverse.app.entity.tech.BiaoZhunGFLXZBDW;
import com.poweruniverse.app.entity.tech.DanWeiJS;
import com.poweruniverse.app.entity.tech.FaMingZLSB;
import com.poweruniverse.app.entity.tech.KaiQiSB;
import com.poweruniverse.app.entity.tech.KeJiCG;
import com.poweruniverse.app.entity.tech.KeJiCGWCRY;
import com.poweruniverse.app.entity.tech.KeJiJL;
import com.poweruniverse.app.entity.tech.KeJiLXXM;
import com.poweruniverse.app.entity.tech.KeJiLXXMYFFYSB;
import com.poweruniverse.app.entity.tech.KeJiLXXMZLGDSB;
import com.poweruniverse.app.entity.tech.KeTiZCYQKJH;
import com.poweruniverse.app.entity.tech.RuanJianZZQ;
import com.poweruniverse.app.entity.tech.RuanJianZZQSQ;
import com.poweruniverse.app.entity.tech.RuanJianZZQSQRZQR;
import com.poweruniverse.app.entity.tech.RuanJianZZQSQSJRY;
import com.poweruniverse.app.entity.tech.ShenBaoBZGFYFFY;
import com.poweruniverse.app.entity.tech.ShenBaoBZGFZLGD;
import com.poweruniverse.app.entity.tech.ShenBaoKJJL;
import com.poweruniverse.app.entity.tech.ShenBaoRJZZQ;
import com.poweruniverse.app.entity.tech.ShenBaoSYZL;
import com.poweruniverse.app.entity.tech.ShenBaoYKJXMGLXSM;
import com.poweruniverse.app.entity.tech.YanShouKJLXXMSB;
import com.poweruniverse.app.entity.tech.ZhuanJiaSBSQ;
import com.poweruniverse.app.entity.tech.ZhuanJiaSBSQZC;
import com.poweruniverse.app.entity.tech.ZhuanLi;
import com.poweruniverse.app.entity.tech.ZhuanLiLX;
import com.poweruniverse.app.entity.tech.ZhuanLiSQ;
import com.poweruniverse.app.entity.tech.ZhuanLiSQKJXMGLFJ;
import com.poweruniverse.app.entity.tech.ZhuanLiSQZLFMR;
import com.poweruniverse.app.entity.tech.ZhuanLiZLFMR;
import com.poweruniverse.app.entity.tech.dm.KeJiXMZLLX;
import com.poweruniverse.app.entity.tech.v.NianDu;
import com.poweruniverse.app.entity.tech.v.YongHu;
import com.poweruniverse.nim.base.message.JSONMessageResult;
import com.poweruniverse.nim.data.action.LoadAction;
import com.poweruniverse.nim.data.entity.EntityI;
import com.poweruniverse.nim.data.entity.sys.GongNengCZ;
import com.poweruniverse.nim.data.service.utils.HibernateSession;
import com.poweruniverse.nim.data.service.utils.HibernateSessionFactory;
import com.poweruniverse.nim.data.service.utils.UserInfoHandler;

import net.sf.json.JSONObject;

public class ZJSBSQLoadBcsbzlAction extends LoadAction {

	@Override
	public JSONMessageResult invoke(UserInfoHandler user, GongNengCZ gongNengCZ, EntityI entity) throws Exception {
		HibernateSession sess = HibernateSessionFactory.getSession(this.getClass());
		ZhuanJiaSBSQ zhuanJiaSBSQ = (ZhuanJiaSBSQ) entity;

		// 第一步 查询共用数据
		YongHu yongHu = (YongHu) sess.createCriteria(YongHu.class).add(Restrictions.eq("yongHuDM", user.getYongHuDM()))
				.uniqueResult();

		if (yongHu != null) {

			Calendar ca = Calendar.getInstance();
			int nowYear = ca.get(Calendar.YEAR);// 年份数值

			Date d = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

			// 年度
			NianDu nd = (NianDu) sess.createCriteria(NianDu.class).add(Restrictions.eq("nianDuDM", nowYear))
					.uniqueResult();
			if (nd == null) {
				return new JSONMessageResult("请联系管理员补充当前年度");
			}
			KaiQiSB kaiQiSB = zhuanJiaSBSQ.getKaiQiSB();

			//获取当前专家申报功能中已经存在的职称集合数据
			List<ZhuanJiaSBSQZC> xcZhuanJiaSBSQZCs = sess.createCriteria(ZhuanJiaSBSQZC.class)
					.add(Restrictions.eq("zhuanJiaSBSQ.id", zhuanJiaSBSQ.getZhuanJiaSBSQDM())).list();
			if(xcZhuanJiaSBSQZCs.size()>0){
				xcZhuanJiaSBSQZCs.get(0).setZhiCheng(zhuanJiaSBSQ.getJiShuZC());
			}else{
				ZhuanJiaSBSQZC zc = new ZhuanJiaSBSQZC();
				zc.assignPrimaryKey();
				zc.setZhiCheng(zhuanJiaSBSQ.getJiShuZC());
				zhuanJiaSBSQ.getZcs().clear();
				zhuanJiaSBSQ.addToZcs(zc);
				sess.update(zhuanJiaSBSQ);
			}
			// 第二步 带入专家基本信息 数据来源《专家基本信息》 （部分用户已在专家库 如果存在 默认带出库信息 流程完成之后再更新库）
			// 迁移到新增节点

			// 第三步 验收科技立项项目 数据来源《科技立项项目》
			addyanShouKJLXXMSBSB(sess, zhuanJiaSBSQ, yongHu, kaiQiSB);

			// 第四步 发明专利申报 数据来源《专利》
			addFaMingZLSB(sess, zhuanJiaSBSQ, yongHu, kaiQiSB);

			// 第五步 标准规范发布实施申报 数据来源《标准规范发布实施》
			addBiaoZhunGFFBSSSB(sess, zhuanJiaSBSQ, yongHu, kaiQiSB);

			// 第六步 实用新型专利申报 数据来源《专利》
			addShenBaoSYZL(sess, zhuanJiaSBSQ, yongHu, kaiQiSB);

			// 软件著作权
			addRuanJianZZQ(sess, zhuanJiaSBSQ, yongHu, kaiQiSB);

			// 第七步 申报科技奖励 数据来源《科技成果》
			addShenBaoKJJL(sess, zhuanJiaSBSQ, yongHu, kaiQiSB);

			// 第八步 近三年内，所负责的各级科技项目（含技术标准）按要求完成资料归档 （第三步 与 第五步的集合）
			addKeJiLXXMZLGDSB(sess, zhuanJiaSBSQ, yongHu, kaiQiSB);// 科技立项项目资料归档申报
			/*
			 * 沈主任解释科技项目（含技术标准）是指以线下得技术标准工作立项的科技项目，与系统现有的标准规范发布实施没有关系。
			 * addShenBaoBZGFZLGD(sess,zhuanJiaSBSQ,yongHu,kaiQiSB);//申报标准规范资料归档
			 */
			// 第九步 近三年内，所负责的各级科技项目（含技术标准）按进度要求完成研发费用归集 （第三步 与 第五步的集合）
			addKeJiLXXMYFFYSB(sess, zhuanJiaSBSQ, yongHu, kaiQiSB);// 科技立项项目研发费用申报

			/*
			 * 沈主任解释科技项目（含技术标准）是指以线下得技术标准工作立项的科技项目，与系统现有的标准规范发布实施没有关系。
			 * addShenBaoBZGFYFFY(sess,zhuanJiaSBSQ,yongHu,kaiQiSB);//申报标准规范研发费用
			 */

		}

		return new JSONMessageResult();
	}

	/*
	 * 沈主任解释科技项目（含技术标准）是指以线下得技术标准工作立项的科技项目，与系统现有的标准规范发布实施没有关系。 //申报标准规范研发费用 private
	 * void addShenBaoBZGFYFFY(HibernateSession sess, ZhuanJiaSBSQ
	 * zhuanJiaSBSQ,YongHu yongHu,KaiQiSB kaiQiSB) {
	 * 
	 * @SuppressWarnings("unchecked") List<BiaoZhunGFFBSS> biaoZhunGFFBSSs =
	 * sess.createCriteria(BiaoZhunGFFBSS.class) .add(Restrictions.
	 * sqlRestriction(" this_.biaoZhunGFLXDM in (select r.biaozhungflxdm from TECH_BiaoZhunGFLXBZRY r where r.yonghudm= '"
	 * +yongHu.getYongHuDM()+"') " +
	 * " and this_.faBuSJ  between to_date( '"+getLatestNYears(kaiQiSB.
	 * getShenBaoKSSJ(),6).getString("kaiShiSJYear")
	 * +"-01-01 00:00:01', 'yyyy-mm-dd hh24:mi:ss')" +
	 * " and to_date('"+getLatestNYears(kaiQiSB.getShenBaoKSSJ(),6).getString(
	 * "jieShuSJ")+" 00:00:01', 'yyyy-mm-dd hh24:mi:ss') "))
	 * .addOrder(Order.asc("biaoZhunGFFBSSDM")) .list();
	 * 
	 * 
	 * if(biaoZhunGFFBSSs.size()>0) { if(zhuanJiaSBSQ.getBzgfyffys().size() > 0 ) {
	 * zhuanJiaSBSQ.getBzgfyffys().clear(); } for (BiaoZhunGFFBSS biaoZhunGFFBSS :
	 * biaoZhunGFFBSSs) { ShenBaoBZGFYFFY shenBaoBZGFYFFY = new ShenBaoBZGFYFFY();
	 * shenBaoBZGFYFFY.assignPrimaryKey();
	 * 
	 * @SuppressWarnings("unchecked") List<BiaoZhunGFLXBZRY> biaoZhunGFLXBZRYs =
	 * sess.createCriteria(BiaoZhunGFLXBZRY.class)
	 * .add(Restrictions.sqlRestriction(" this_.biaoZhunGFLXDM ="+biaoZhunGFFBSS.
	 * getBiaoZhunGFLX().getBiaoZhunGFLXDM()+""))
	 * .addOrder(Order.asc("biaoZhunGFLXBZRYDM")) .list();
	 * 
	 * Integer brpm = null ; Integer dfpm = 0; // if(biaoZhunGFLXBZRYs.size()>0) {
	 * // for (int i = 0 ; i <biaoZhunGFLXBZRYs.size() ; i ++) { // BiaoZhunGFLXBZRY
	 * zhunGFLXBZRY = biaoZhunGFLXBZRYs.get(i); // if(zhunGFLXBZRY.getYongHu() !=
	 * null) { // if(zhunGFLXBZRY.getYongHu().getYongHuDM() == yongHu.getYongHuDM())
	 * { // brpm = Integer.parseInt(zhunGFLXBZRY.getPaiMing()); // } // } //
	 * if(zhunGFLXBZRY.getShiFouWDW()) { // continue; // } // YongHu yonghu1 =
	 * (YongHu) sess.load(YongHu.class, zhunGFLXBZRY.getYongHu().getYongHuDM()); //
	 * if(yonghu1.getYongHuZT().getYongHuZTDM() != 2) { // } // dfpm +=1; // } // }
	 * 
	 * @SuppressWarnings("unchecked") List<BiaoZhunGFLXZBDW> zhunGFLXZBDWs =
	 * sess.createCriteria(BiaoZhunGFLXZBDW.class)
	 * .add(Restrictions.sqlRestriction(" this_.biaoZhunGFLXDM ="+biaoZhunGFFBSS.
	 * getBiaoZhunGFLX().getBiaoZhunGFLXDM()+" and this_.bumendm = "+yongHu.
	 * getYongHuDM()+"")) .addOrder(Order.asc("biaoZhunGFLXZBDWDM")) .list();
	 * shenBaoBZGFYFFY.setXiangMuBH(biaoZhunGFFBSS.getBiaoZhunBH());//标准编号
	 * shenBaoBZGFYFFY.setBiaoZhunGFLX(biaoZhunGFFBSS.getBiaoZhunGFLX());;//标准规范立项
	 * shenBaoBZGFYFFY.setSuoShuJB(biaoZhunGFFBSS.getSuoShuJB());//所属级别
	 * if(brpm!=null){ shenBaoBZGFYFFY.setGeRenPM(dfpm.toString());//本人项目排名 }
	 * shenBaoBZGFYFFY.setZhuanJiaSBSQ(zhuanJiaSBSQ);
	 * 
	 * zhuanJiaSBSQ.getBzgfyffys().add(shenBaoBZGFYFFY); } }
	 * 
	 * }
	 */
	// 科技立项项目研发费用申报
	private void addKeJiLXXMYFFYSB(HibernateSession sess, ZhuanJiaSBSQ zhuanJiaSBSQ, YongHu yongHu, KaiQiSB kaiQiSB) {
		// 需优化为使用用户做关联 不能使用
		@SuppressWarnings("unchecked")
		List<KeJiLXXM> jiLXXMsys = sess.createCriteria(KeJiLXXM.class).add(Restrictions.sqlRestriction(
				"processinstanceended=1 and shanchuzt=0 and processinstanceterminated=0 and  this_.keJiLXXMdm in (select distinct(jh.kejilxxmdm) from TECH_KeTiZCYQKJH jh where jh.yonghudm='"
						+ yongHu.getYongHuDM() + "') " + " and this_.shijijtyssj  between to_date( '"
						+ getLatestNYears(kaiQiSB.getShenBaoKSSJ(), 3).getString("kaiShiSJYear")
						+ "-01-01 00:00:01', 'yyyy-mm-dd hh24:mi:ss')" + " and to_date('"
						+ getLatestNYears(kaiQiSB.getShenBaoKSSJ(), 3).getString("jieShuSJ")
						+ " 00:00:01', 'yyyy-mm-dd hh24:mi:ss') " + " and this_.xiangmuztdm =4"))
				.addOrder(Order.asc("keJiLXXMDM")).list();

		if (jiLXXMsys.size() > 0) {
			//获取当前专家申报功能中已经存在的验收科技立项项目集合数据
			List<KeJiLXXMYFFYSB> xcKeJiLXXMYFFYSBs = sess.createCriteria(KeJiLXXMYFFYSB.class)
					.add(Restrictions.eq("zhuanJiaSBSQ.id", zhuanJiaSBSQ.getZhuanJiaSBSQDM())).list();
//			if (zhuanJiaSBSQ.getKjlxxmyffys().size() > 0) {
//				zhuanJiaSBSQ.getKjlxxmyffys().clear();
//			}
			for (KeJiLXXM keJiLXXM : jiLXXMsys) {

				@SuppressWarnings("unchecked")
				// 李睿增加，获取项目课题组成员中的排名
				List<KeTiZCYQKJH> keTiZCYQKJHs = sess.createCriteria(KeTiZCYQKJH.class)
						.add(Restrictions.sqlRestriction(" this_.keJiLXXMDM =" + keJiLXXM.getKeJiLXXMDM() + ""))
						.add(Restrictions.eq("yongHu.id", yongHu.getYongHuDM())).addOrder(Order.asc("paiMing")).list();
				
				Integer brpm =Integer.parseInt(keTiZCYQKJHs.get(0).getPaiMing()) ;
				
				// 李睿：获取申请人排名之前，第一名之后的所有党管干部
				List<KeTiZCYQKJH> dggbsl = sess.createCriteria(KeTiZCYQKJH.class)
						.add(Restrictions.sqlRestriction(" this_.keJiLXXMDM =" + keJiLXXM.getKeJiLXXMDM() + ""))
						.add(Restrictions.lt("paiMing", keTiZCYQKJHs.get(0).getPaiMing()))// 排名数值小于申报人排名的所有数据
						.add(Restrictions.gt("paiMing", "1"))// 排名第一名的人员，无论党管干部也要算到得分排名中
						.add(Restrictions.eq("shiFouWDW", true))// 外单位
						//.add(Restrictions.eq("shiFouDGGB", true))// 是否党管干部						
						.addOrder(Order.asc("paiMing"))// 李睿帮助修改错误名称
						.list();

				Integer dfpm = brpm - dggbsl.size();

					int ycz = 0;// 标记现在这个满足条件的科技项目是否在验收科技立项项目集合中。
					for (KeJiLXXMYFFYSB xykjxmfys : xcKeJiLXXMYFFYSBs) {
						if (xykjxmfys.getKeJiLXXM().getKeJiLXXMDM() == keJiLXXM.getKeJiLXXMDM()) {

							xykjxmfys.setXiangMuBH(keJiLXXM.getXiangMuBH());// 项目编号
							xykjxmfys.setKeJiLXXM(keJiLXXM);// 科技立项项目
							xykjxmfys.setSuoShuJB(keJiLXXM.getXiangMuJB());// 项目级别
							xykjxmfys.setDangNianYGJFY(keJiLXXM.getKeTiZJE());// 项目预算总额

							if (brpm != null) {
								xykjxmfys.setGeRenPM(brpm.toString());// 本人项目排名
							}
							ycz = 1;
							break;
						}
					}
					
					if(ycz == 0){

						KeJiLXXMYFFYSB keJiLXXMYFFYSB = new KeJiLXXMYFFYSB();
						keJiLXXMYFFYSB.assignPrimaryKey();
						keJiLXXMYFFYSB.setXiangMuBH(keJiLXXM.getXiangMuBH());// 项目编号
						keJiLXXMYFFYSB.setKeJiLXXM(keJiLXXM);// 科技立项项目
						keJiLXXMYFFYSB.setSuoShuJB(keJiLXXM.getXiangMuJB());// 项目级别
						keJiLXXMYFFYSB.setDangNianYGJFY(keJiLXXM.getKeTiZJE());// 项目预算总额
						if (brpm != null) {
							keJiLXXMYFFYSB.setGeRenPM(brpm.toString());// 本人项目排名
						}
						keJiLXXMYFFYSB.setKouFenPM(dfpm.toString());
						keJiLXXMYFFYSB.setZhuanJiaSBSQ(zhuanJiaSBSQ);
						zhuanJiaSBSQ.getKjlxxmyffys().add(keJiLXXMYFFYSB);
					}
				
			}
		}
	}

	/*
	 * 沈主任解释科技项目（含技术标准）是指以线下得技术标准工作立项的科技项目，与系统现有的标准规范发布实施没有关系。 //申报标准规范资料归档 private
	 * void addShenBaoBZGFZLGD(HibernateSession sess, ZhuanJiaSBSQ
	 * zhuanJiaSBSQ,YongHu yongHu,KaiQiSB kaiQiSB) {
	 * 
	 * @SuppressWarnings("unchecked") List<BiaoZhunGFFBSS> biaoZhunGFFBSSs =
	 * sess.createCriteria(BiaoZhunGFFBSS.class) .add(Restrictions.
	 * sqlRestriction(" this_.biaoZhunGFLXDM in (select r.biaozhungflxdm from TECH_BiaoZhunGFLXBZRY r where r.yonghudm= '"
	 * +yongHu.getYongHuDM()+"') " +
	 * " and this_.faBuSJ  between to_date( '"+getLatestNYears(kaiQiSB.
	 * getShenBaoKSSJ(),6).getString("kaiShiSJYear")
	 * +"-01-01 00:00:01', 'yyyy-mm-dd hh24:mi:ss')" +
	 * " and to_date('"+getLatestNYears(kaiQiSB.getShenBaoKSSJ(),6).getString(
	 * "jieShuSJ")+" 00:00:01', 'yyyy-mm-dd hh24:mi:ss') "))
	 * .addOrder(Order.asc("biaoZhunGFFBSSDM")) .list();
	 * 
	 * 
	 * if(biaoZhunGFFBSSs.size()>0) { if(zhuanJiaSBSQ.getBzgfzlgds().size() > 0 ) {
	 * zhuanJiaSBSQ.getBzgfzlgds().clear(); } for (BiaoZhunGFFBSS biaoZhunGFFBSS :
	 * biaoZhunGFFBSSs) { ShenBaoBZGFZLGD shenBaoBZGFZLGD = new ShenBaoBZGFZLGD();
	 * shenBaoBZGFZLGD.assignPrimaryKey();
	 * 
	 * @SuppressWarnings("unchecked") List<BiaoZhunGFLXBZRY> biaoZhunGFLXBZRYs =
	 * sess.createCriteria(BiaoZhunGFLXBZRY.class)
	 * .add(Restrictions.sqlRestriction(" this_.biaoZhunGFLXDM ="+biaoZhunGFFBSS.
	 * getBiaoZhunGFLX().getBiaoZhunGFLXDM()+""))
	 * .addOrder(Order.asc("biaoZhunGFLXBZRYDM")) .list();
	 * 
	 * Integer brpm = null ; Integer dfpm = 0; // if(biaoZhunGFLXBZRYs.size()>0) {
	 * // for (int i = 0 ; i <biaoZhunGFLXBZRYs.size() ; i ++) { // BiaoZhunGFLXBZRY
	 * zhunGFLXBZRY = biaoZhunGFLXBZRYs.get(i); // if(zhunGFLXBZRY.getYongHu() !=
	 * null) { // if(zhunGFLXBZRY.getYongHu().getYongHuDM() == yongHu.getYongHuDM())
	 * { // brpm = Integer.parseInt(zhunGFLXBZRY.getPaiMing()); // } // } //
	 * if(zhunGFLXBZRY.getShiFouWDW()) { // continue; // } // YongHu yonghu1 =
	 * (YongHu) sess.load(YongHu.class, zhunGFLXBZRY.getYongHu().getYongHuDM()); //
	 * if(yonghu1.getYongHuZT().getYongHuZTDM() != 2) { // } // dfpm +=1; // } // }
	 * 
	 * @SuppressWarnings("unchecked") List<BiaoZhunGFLXZBDW> zhunGFLXZBDWs =
	 * sess.createCriteria(BiaoZhunGFLXZBDW.class)
	 * .add(Restrictions.sqlRestriction(" this_.biaoZhunGFLXDM ="+biaoZhunGFFBSS.
	 * getBiaoZhunGFLX().getBiaoZhunGFLXDM()+" and this_.bumendm = "+yongHu.
	 * getYongHuDM()+"")) .addOrder(Order.asc("biaoZhunGFLXZBDWDM")) .list();
	 * shenBaoBZGFZLGD.setXiangMuBH(biaoZhunGFFBSS.getBiaoZhunBH());//标准编号
	 * shenBaoBZGFZLGD.setBiaoZhunGFLX(biaoZhunGFFBSS.getBiaoZhunGFLX());;//标准规范立项
	 * shenBaoBZGFZLGD.setSuoShuJB(biaoZhunGFFBSS.getSuoShuJB());//所属级别
	 * shenBaoBZGFZLGD.setGeRenPM(dfpm);
	 * shenBaoBZGFZLGD.setZhuanJiaSBSQ(zhuanJiaSBSQ);
	 * 
	 * zhuanJiaSBSQ.getBzgfzlgds().add(shenBaoBZGFZLGD); } }
	 * 
	 * }
	 * 
	 */
	// 科技立项项目资料归档申报
	private void addKeJiLXXMZLGDSB(HibernateSession sess, ZhuanJiaSBSQ zhuanJiaSBSQ, YongHu yongHu, KaiQiSB kaiQiSB) {
		// 需优化为使用用户做关联 不能使用
		@SuppressWarnings("unchecked")
		List<KeJiLXXM> jiLXXMsys = sess.createCriteria(KeJiLXXM.class).add(Restrictions.sqlRestriction(
				"processinstanceended=1 and shanchuzt=0 and processinstanceterminated=0 and  this_.keJiLXXMdm in (select distinct(jh.kejilxxmdm) from TECH_KeTiZCYQKJH jh where jh.yonghudm='"
						+ yongHu.getYongHuDM() + "') " + " and this_.shijijtyssj  between to_date( '"
						+ getLatestNYears(kaiQiSB.getShenBaoKSSJ(), 3).getString("kaiShiSJYear")//李睿改为3年
						+ "-01-01 00:00:01', 'yyyy-mm-dd hh24:mi:ss')" + " and to_date('"
						+ getLatestNYears(kaiQiSB.getShenBaoKSSJ(), 3).getString("jieShuSJ")//李睿改为3年
						+ " 00:00:01', 'yyyy-mm-dd hh24:mi:ss') " + " and this_.xiangmuztdm =4"))
				.addOrder(Order.asc("keJiLXXMDM")).list();

		if (jiLXXMsys.size() > 0) {
			//获取当前专家申报功能中已经存在的验收科技立项项目集合数据
			List<KeJiLXXMZLGDSB> xcKeJiLXXMZLGDSBs = sess.createCriteria(KeJiLXXMZLGDSB.class)
					.add(Restrictions.eq("zhuanJiaSBSQ.id", zhuanJiaSBSQ.getZhuanJiaSBSQDM())).list();
//			if (zhuanJiaSBSQ.getKjlxxmzlgds().size() > 0) {
//				zhuanJiaSBSQ.getKjlxxmzlgds().clear();
//			}
			for (KeJiLXXM keJiLXXM : jiLXXMsys) {

				@SuppressWarnings("unchecked")
//				List<KeTiZCYQKJH> keTiZCYQKJHs = sess.createCriteria(KeTiZCYQKJH.class)
//						.add(Restrictions.sqlRestriction(" this_.keJiLXXMDM =" + keJiLXXM.getKeJiLXXMDM() + ""))
//						.addOrder(Order.asc("keTiZCYQKJHDM")).list();

				// 李睿增加，获取项目课题组成员中的排名
				List<KeTiZCYQKJH> keTiZCYQKJHs = sess.createCriteria(KeTiZCYQKJH.class)
						.add(Restrictions.sqlRestriction(" this_.keJiLXXMDM =" + keJiLXXM.getKeJiLXXMDM() + ""))
						.add(Restrictions.eq("yongHu.id", yongHu.getYongHuDM())).addOrder(Order.asc("paiMing")).list();
				
				Integer brpm =Integer.parseInt(keTiZCYQKJHs.get(0).getPaiMing()) ;
				// 李睿：获取申请人排名之前，第一名之后的所有党管干部
				List<KeTiZCYQKJH> dggbsl = sess.createCriteria(KeTiZCYQKJH.class)
						.add(Restrictions.sqlRestriction(" this_.keJiLXXMDM =" + keJiLXXM.getKeJiLXXMDM() + ""))
						.add(Restrictions.lt("paiMing", keTiZCYQKJHs.get(0).getPaiMing()))// 排名数值小于申报人排名的所有数据
						.add(Restrictions.gt("paiMing", "1"))// 排名第一名的人员，无论党管干部也要算到得分排名中
						.add(Restrictions.eq("shiFouWDW", true))// 外单位
						//.add(Restrictions.eq("shiFouDGGB", true))// 是否党管干部						
						.addOrder(Order.asc("paiMing"))// 李睿帮助修改错误名称
						.list();

				Integer dfpm = brpm - dggbsl.size();

				  //李睿：计划归档时间为实际结题验收时间三个月之后
		        Calendar rightNow = Calendar.getInstance();  
		        rightNow.setTime(keJiLXXM.getShiJiJTYSSJ());  
		        rightNow.add(Calendar.MONTH, 3); 
		        Date jhgdsj= rightNow.getTime(); //计划归档时间
		        
				int ycz = 0;// 标记现在这个满足条件的科技项目是否在验收科技立项项目集合中。
				for (KeJiLXXMZLGDSB xyxmgds : xcKeJiLXXMZLGDSBs) {
					if (xyxmgds.getKeJiLXXM().getKeJiLXXMDM() == keJiLXXM.getKeJiLXXMDM()) {
						
						xyxmgds.setXiangMuBH(keJiLXXM.getXiangMuBH());// 项目编号
						xyxmgds.setKeJiLXXM(keJiLXXM);// 科技立项项目
						xyxmgds.setSuoShuJB(keJiLXXM.getXiangMuJB());// 项目级别
						xyxmgds.setJiHuaGDSJ(jhgdsj);//计划归档时间
						xyxmgds.setShiJiJTSJ(keJiLXXM.getShiJiJTYSSJ());//实际结题验收时间
						if (brpm != null) {
							xyxmgds.setGeRenPM(brpm.toString());// 本人项目排名
						}
						ycz = 1;
						break;
					}
				}
				
				if(ycz == 0){

					KeJiLXXMZLGDSB keJiLXXMZLGDSB = new KeJiLXXMZLGDSB();
					keJiLXXMZLGDSB.assignPrimaryKey();
					keJiLXXMZLGDSB.setXiangMuBH(keJiLXXM.getXiangMuBH());// 项目编号
					keJiLXXMZLGDSB.setKeJiLXXM(keJiLXXM);// 科技立项项目
					keJiLXXMZLGDSB.setSuoShuJB(keJiLXXM.getXiangMuJB());// 项目级别
					keJiLXXMZLGDSB.setJiHuaGDSJ(jhgdsj);//计划归档时间
					keJiLXXMZLGDSB.setShiJiJTSJ(keJiLXXM.getShiJiJTYSSJ());//实际结题验收时间
					if (brpm != null) {
						keJiLXXMZLGDSB.setGeRenPM(brpm.toString());// 本人项目排名
					}
					keJiLXXMZLGDSB.setKouFenPM(dfpm.toString());
					keJiLXXMZLGDSB.setZhuanJiaSBSQ(zhuanJiaSBSQ);
					zhuanJiaSBSQ.getKjlxxmzlgds().add(keJiLXXMZLGDSB);
				}
		        

			}
		}
	}

	// 申报科技奖励
	private void addShenBaoKJJL(HibernateSession sess, ZhuanJiaSBSQ zhuanJiaSBSQ, YongHu yongHu, KaiQiSB kaiQiSB) {
		@SuppressWarnings("unchecked")
		

		List<KeJiLXXM> kJiLXXMs = sess.createCriteria(KeJiLXXM.class)
				.add(Restrictions.sqlRestriction(
						"processinstanceended=1 and shanchuzt=0 and processinstanceterminated=0 and  this_.keJiLXXMDM in (select jlglxm.kejilxxmdm from tech_kejijlglkjxm jlglxm) and  this_.keJiLXXMDM in (select cy.kejilxxmdm from tech_ketizcyqkjh cy where cy.yonghudm = "
								+ yongHu.getYongHuDM() + ") ")).addOrder(Order.asc("keJiLXXMDM")).list();
		if (kJiLXXMs.size() > 0) {
			//获取当前专家申报功能中已经存在的科技奖励集合数据
			List<ShenBaoKJJL> xcShenBaoKJJLs = sess.createCriteria(ShenBaoKJJL.class)
					.add(Restrictions.eq("zhuanJiaSBSQ.id", zhuanJiaSBSQ.getZhuanJiaSBSQDM())).list();
			
//			if (zhuanJiaSBSQ.getKhkjjls().size() > 0) {
//				zhuanJiaSBSQ.getKhkjjls().clear();
//			}
			for (KeJiLXXM xm : kJiLXXMs) {

				@SuppressWarnings("unchecked")
				List<KeJiJL> keJiJLs = sess.createCriteria(KeJiJL.class).add(Restrictions.sqlRestriction(
						"processinstanceended=1 and shanchuzt=0 and processinstanceterminated=0 and this_.keJiJLDM in (select jlglxm.kejijldm from tech_kejijlglkjxm jlglxm where jlglxm.kejilxxmdm = "
								+ xm.getKeJiLXXMDM() + ") and this_.huoJiangNDDM  between "
								+ getLatestNYears(kaiQiSB.getShenBaoKSSJ(), 3).getString("kaiShiSJYear") + " and "
								+ getLatestNYears(kaiQiSB.getShenBaoKSSJ(), 0).getString("kaiShiSJYear") + ""))
						// + " and
						// getLatestNYears(kaiQiSB.getShenBaoKSSJ(),3).getString("kaiShiSJYear")"""
						.addOrder(Order.desc("huoJiangND")).addOrder(Order.asc("jiangLiLB"))
						.addOrder(Order.asc("jiangLiDJ"))// 制度要求是近三年内一个成果的最新最高奖励获取，先根据获奖年度排序，同一获奖年度最高奖励类别排第一，同一类别，最高等级排第一
						.list();

				@SuppressWarnings("unchecked")
				List<KeTiZCYQKJH> keTiZCYQKJHs = sess.createCriteria(KeTiZCYQKJH.class)
						.add(Restrictions.sqlRestriction(" this_.keJiLXXMDM = " + xm.getKeJiLXXMDM() + "and this_.yongHuDM = " + yongHu.getYongHuDM() + ""))
						.addOrder(Order.asc("paiMing"))// 按照成果完成人员的排序字段来排序
						.list();
				Integer brpm = Integer.parseInt(keTiZCYQKJHs.get(0).getPaiMing());
				Integer dfpm = 0;
				if (keJiJLs.size() > 0) {

						int ycz = 0;// 标记现在这个满足条件的科技项目是否在验收科技立项项目集合中。
						for (ShenBaoKJJL xyjls : xcShenBaoKJJLs) {
							if (xyjls.getXinHuoDJL().getKeJiJLDM() == keJiCG.getKeJiCGDM()) {

//								// 取第一个值就是最近年度，最高级别、最高等级的值
//								xyjls.setXinHuoDJL(keJiJLs.get(0));
//								xyjls.setXinHuoJND(keJiJLs.get(0).getHuoJiangND());
//								xyjls.setJiangXiangDJ(keJiJLs.get(0).getJiangLiDJ());
//
//								xyjls.setSheJiangJG(keJiJLs.get(0).getSheJiangJG());
								xyjls.setKeJiCG(keJiCG);// 科技成果
								xyjls.setChengGuoZPM(brpm);// 本人排名
								xyjls.setJiSuanDFPM(dfpm);// 得分排名
								ycz = 1;
								break;
							}
						}
						
						if(ycz == 0){
							ShenBaoKJJL shenBaoKJJL = new ShenBaoKJJL();
							shenBaoKJJL.assignPrimaryKey();
							// 取第一个值就是最近年度，最高级别、最高等级的值
							shenBaoKJJL.setXinHuoDJL(keJiJLs.get(0));
							shenBaoKJJL.setXinHuoJND(keJiJLs.get(0).getHuoJiangND());
							shenBaoKJJL.setJiangXiangDJ(keJiJLs.get(0).getJiangLiDJ());
							shenBaoKJJL.setSheJiangJG(keJiJLs.get(0).getSheJiangJG());
							shenBaoKJJL.setKeJiCG(keJiCG);// 科技成果
							shenBaoKJJL.setChengGuoZPM(brpm);// 本人排名
							shenBaoKJJL.setJiSuanDFPM(dfpm);// 得分排名
							shenBaoKJJL.setZhuanJiaSBSQ(zhuanJiaSBSQ);
							zhuanJiaSBSQ.getKhkjjls().add(shenBaoKJJL);
							
						}


				}
			}
		}		
		List<KeJiCG> jiCGs = sess.createCriteria(KeJiCG.class)
				.add(Restrictions.sqlRestriction(
						"processinstanceended=1 and shanchuzt=0 and processinstanceterminated=0 and  this_.keJiCGDM in (select ry.kejicgdm  from TECH_KeJiCGWCRY ry where ry.yonghudm = "
								+ yongHu.getYongHuDM() + ") "
				/*
				 * 需求为获取近三年的科技奖励，科技成果作为科技奖励的依据，不限制时间，只要是其成果在近三年得到奖励即可，需要先获取该申报人的所有科技成果，
				 * 再检查该科技成果是否有近三年的科技奖励 +
				 * " and this_.JIANDINGRQ  between to_date( '"+getLatestNYears(kaiQiSB.
				 * getShenBaoKSSJ(),3).getString("kaiShiSJYear")
				 * +"-01-01 00:00:01', 'yyyy-mm-dd hh24:mi:ss')" +
				 * " and to_date('"+getLatestNYears(kaiQiSB.getShenBaoKSSJ(),6).getString(
				 * "jieShuSJ")+" 00:00:01', 'yyyy-mm-dd hh24:mi:ss') "
				 */)).addOrder(Order.asc("keJiCGDM")).list();

		if (jiCGs.size() > 0) {
			//获取当前专家申报功能中已经存在的科技奖励集合数据
			List<ShenBaoKJJL> xcShenBaoKJJLs = sess.createCriteria(ShenBaoKJJL.class)
					.add(Restrictions.eq("zhuanJiaSBSQ.id", zhuanJiaSBSQ.getZhuanJiaSBSQDM())).list();
			
//			if (zhuanJiaSBSQ.getKhkjjls().size() > 0) {
//				zhuanJiaSBSQ.getKhkjjls().clear();
//			}
			for (KeJiCG keJiCG : jiCGs) {

				@SuppressWarnings("unchecked")
				List<KeJiJL> keJiJLs = sess.createCriteria(KeJiJL.class).add(Restrictions.sqlRestriction(
						"processinstanceended=1 and shanchuzt=0 and processinstanceterminated=0 and this_.keJiJLDM in (select gljlcg.kejijldm  from TECH_KeJiJLGLKJCG gljlcg where gljlcg.kejicgdm = "
								+ keJiCG.getKeJiCGDM() + ") and this_.huoJiangNDDM  between "
								+ getLatestNYears(kaiQiSB.getShenBaoKSSJ(), 3).getString("kaiShiSJYear") + " and "
								+ getLatestNYears(kaiQiSB.getShenBaoKSSJ(), 0).getString("kaiShiSJYear") + ""))
						// + " and
						// getLatestNYears(kaiQiSB.getShenBaoKSSJ(),3).getString("kaiShiSJYear")"""
						.addOrder(Order.desc("huoJiangND")).addOrder(Order.asc("jiangLiLB"))
						.addOrder(Order.asc("jiangLiDJ"))// 制度要求是近三年内一个成果的最新最高奖励获取，先根据获奖年度排序，同一获奖年度最高奖励类别排第一，同一类别，最高等级排第一
						.list();

				@SuppressWarnings("unchecked")
				List<KeJiCGWCRY> keJiCGWCRYs = sess.createCriteria(KeJiCGWCRY.class)
						.add(Restrictions.sqlRestriction(" this_.keJiCGDM = " + keJiCG.getKeJiCGDM() + "and this_.yongHuDM = " + yongHu.getYongHuDM() + ""))
						.addOrder(Order.asc("paiXu"))// 按照成果完成人员的排序字段来排序
						.list();
				Integer brpm = Integer.parseInt(keJiCGWCRYs.get(0).getPaiXu());
				Integer dfpm = 0;
				if (keJiJLs.size() > 0) {

						int ycz = 0;// 标记现在这个满足条件的科技项目是否在验收科技立项项目集合中。
						for (ShenBaoKJJL xyjls : xcShenBaoKJJLs) {
							if (xyjls.getKeJiCG().getKeJiCGDM() == keJiCG.getKeJiCGDM()) {

//								// 取第一个值就是最近年度，最高级别、最高等级的值
//								xyjls.setXinHuoDJL(keJiJLs.get(0));
//								xyjls.setXinHuoJND(keJiJLs.get(0).getHuoJiangND());
//								xyjls.setJiangXiangDJ(keJiJLs.get(0).getJiangLiDJ());
//
//								xyjls.setSheJiangJG(keJiJLs.get(0).getSheJiangJG());
								xyjls.setKeJiCG(keJiCG);// 科技成果
								xyjls.setChengGuoZPM(brpm);// 本人排名
								xyjls.setJiSuanDFPM(dfpm);// 得分排名
								ycz = 1;
								break;
							}
						}
						
						if(ycz == 0){
							ShenBaoKJJL shenBaoKJJL = new ShenBaoKJJL();
							shenBaoKJJL.assignPrimaryKey();
							// 取第一个值就是最近年度，最高级别、最高等级的值
							shenBaoKJJL.setXinHuoDJL(keJiJLs.get(0));
							shenBaoKJJL.setXinHuoJND(keJiJLs.get(0).getHuoJiangND());
							shenBaoKJJL.setJiangXiangDJ(keJiJLs.get(0).getJiangLiDJ());
							shenBaoKJJL.setSheJiangJG(keJiJLs.get(0).getSheJiangJG());
							shenBaoKJJL.setKeJiCG(keJiCG);// 科技成果
							shenBaoKJJL.setChengGuoZPM(brpm);// 本人排名
							shenBaoKJJL.setJiSuanDFPM(dfpm);// 得分排名
							shenBaoKJJL.setZhuanJiaSBSQ(zhuanJiaSBSQ);
							zhuanJiaSBSQ.getKhkjjls().add(shenBaoKJJL);
							
						}


				}
			}
		}
	}

	// 实用新型专利申报
	private void addShenBaoSYZL(HibernateSession sess, ZhuanJiaSBSQ zhuanJiaSBSQ, YongHu yongHu, KaiQiSB kaiQiSB) {
		@SuppressWarnings("unchecked")
		List<ZhuanLi> zhuanLisys = sess.createCriteria(ZhuanLi.class).add(Restrictions.sqlRestriction(
				"processinstanceended=1 and shanchuzt=0 and processinstanceterminated=0 and  this_.zhuanlilxdm = 3 and this_.zhuanLiDM in (select distinct(fml.zhuanLiDM) from TECH_ZhuanLiZLFMR fml where fml.yonghudm = '"
						+ yongHu.getYongHuDM() + "')" + " and this_.shouquanggr  between to_date( '"
						+ getLatestNYears(kaiQiSB.getShenBaoKSSJ(), 6).getString("kaiShiSJYear")
						+ "-01-01 00:00:01', 'yyyy-mm-dd hh24:mi:ss')" + " and to_date('"
						+ getLatestNYears(kaiQiSB.getShenBaoKSSJ(), 6).getString("jieShuSJ")
						+ " 00:00:01', 'yyyy-mm-dd hh24:mi:ss') "))
				.addOrder(Order.asc("zhuanLiDM")).list();

		if (zhuanLisys.size() > 0) {
			//获取当前专家申报功能中已经存在的实用新型专利集合数据
			List<ShenBaoSYZL> xcShenBaoSYZLs = sess.createCriteria(ShenBaoSYZL.class)
					.add(Restrictions.eq("zhuanJiaSBSQ.id", zhuanJiaSBSQ.getZhuanJiaSBSQDM())).list();
			
//			if (zhuanJiaSBSQ.getKhsyzls().size() > 0) {
//				zhuanJiaSBSQ.getKhsyzls().clear();
//			}
			for (ZhuanLi li : zhuanLisys) {

				@SuppressWarnings("unchecked")
				// 李睿：先获取申报人在专利发明人员的排名，再去把该排名之前的人员筛选党管干部，减去这类之后就是得分排名
				List<ZhuanLiZLFMR> zhuanLiZLFMRs = sess.createCriteria(ZhuanLiZLFMR.class)
						.add(Restrictions.sqlRestriction(" this_.zhuanLiDM =" + li.getZhuanLiDM()
								+ "and this_.yongHuDM = " + yongHu.getYongHuDM() + ""))
						.addOrder(Order.asc("zhuanLiZLFMRDM")).list();

				Integer brpm = Integer.parseInt(zhuanLiZLFMRs.get(0).getPaiXu());// 获得本人排名

				// 李睿：获取申请人排名之前，第一名之后的所有党管干部
				List<ZhuanLiSQZLFMR> dggbsl = sess.createCriteria(ZhuanLiSQZLFMR.class)
//						.add(Restrictions
//								.sqlRestriction(" this_.zhuanLiSQDM =" + li.getZhuanLiSQ().getZhuanLiSQDM() + ""))
						.add(Restrictions.lt("paiXu", zhuanLiZLFMRs.get(0).getPaiXu()))// 排名数值小于申报人排名的所有数据
						.add(Restrictions.gt("paiXu", "1"))// 排名第一名的人员，无论党管干部也要算到得分排名中
						.add(Restrictions.eq("shiFouDGGB", true))// 是否党管干部
						.addOrder(Order.asc("zhuanLiSQZLFMRDM"))// 李睿帮助修改错误名称
						.list();

				Integer dfpm = brpm - dggbsl.size();
				int ycz = 0;// 标记现在这个满足条件的科技项目是否在验收科技立项项目集合中。
				for (ShenBaoSYZL xcsyxxs : xcShenBaoSYZLs) {
					if (xcsyxxs.getZhuanLi().getZhuanLiDM() == li.getZhuanLiDM()) {
//						ZhuanLiSQ sq = (ZhuanLiSQ) sess.load(ZhuanLiSQ.class, li.getZhuanLiSQ().getZhuanLiSQDM());
						xcsyxxs.setShouQuanGGR(li.getShouQuanGGR());// 授权公告日
						xcsyxxs.setZhuanLiQR(li.getZhuanLiQRWB());// 专利权人
						xcsyxxs.setZhuanLiH(li.getZhuanLiH());// 专利号
						xcsyxxs.setDeFenJSPM(dfpm);// 得分排名
						xcsyxxs.setShouQuanPM(brpm);// 授权排名
//						if (sq.getKjxmglfjs().size() > 0) {
//							xcsyxxs.getYkjxmglxsms().clear();
//							for (ZhuanLiSQKJXMGLFJ zhuanLiSQKJXMGLFJ : sq.getKjxmglfjs()) {
//								ShenBaoYKJXMGLXSM shenBaoYKJXMGLXSM = new ShenBaoYKJXMGLXSM();
//								shenBaoYKJXMGLXSM.assignPrimaryKey();
//								shenBaoYKJXMGLXSM.setFuJian(zhuanLiSQKJXMGLFJ.getFuJian());
//								shenBaoYKJXMGLXSM.setShenBaoSYZL(xcsyxxs);
//								;
//								xcsyxxs.getYkjxmglxsms().add(shenBaoYKJXMGLXSM);
//							}
//						}
						xcsyxxs.setZhiShiCQLX(li.getZhuanLiLX());// 专利类型
						ycz = 1;
						break;
					}
				}
				
				if(ycz == 0){
					ShenBaoSYZL shenBaoSYZL = new ShenBaoSYZL();
					shenBaoSYZL.assignPrimaryKey();
//					ZhuanLiSQ sq = (ZhuanLiSQ) sess.load(ZhuanLiSQ.class, li.getZhuanLiSQ().getZhuanLiSQDM());
					shenBaoSYZL.setZhuanLi(li);
					// 专利
					shenBaoSYZL.setShouQuanGGR(li.getShouQuanGGR());// 授权公告日
					shenBaoSYZL.setZhuanLiQR(li.getZhuanLiQRWB());// 专利权人
					shenBaoSYZL.setZhuanLiH(li.getZhuanLiH());// 专利号
					shenBaoSYZL.setDeFenJSPM(dfpm);// 得分排名
					shenBaoSYZL.setShouQuanPM(brpm);// 授权排名
//					if (sq.getKjxmglfjs().size() > 0) {
////						shenBaoSYZL.getYkjxmglxsms().clear();
//						for (ZhuanLiSQKJXMGLFJ zhuanLiSQKJXMGLFJ : sq.getKjxmglfjs()) {
//							ShenBaoYKJXMGLXSM shenBaoYKJXMGLXSM = new ShenBaoYKJXMGLXSM();
//							shenBaoYKJXMGLXSM.assignPrimaryKey();
//							shenBaoYKJXMGLXSM.setFuJian(zhuanLiSQKJXMGLFJ.getFuJian());
//							shenBaoYKJXMGLXSM.setShenBaoSYZL(shenBaoSYZL);
//							;
//							shenBaoSYZL.getYkjxmglxsms().add(shenBaoYKJXMGLXSM);
//						}
//					}
					shenBaoSYZL.setZhiShiCQLX(li.getZhuanLiLX());// 专利类型
					shenBaoSYZL.setZhuanJiaSBSQ(zhuanJiaSBSQ);
					zhuanJiaSBSQ.getKhsyzls().add(shenBaoSYZL);
					
				}
				// if(zhuanLiSQZLFMRs.size()>0) {
				// for (int i = 0 ; i <zhuanLiSQZLFMRs.size() ; i ++) {
				// ZhuanLiSQZLFMR sqzlfmr = zhuanLiSQZLFMRs.get(i);
				// if(sqzlfmr.getYongHu() != null) {
				// if(sqzlfmr.getYongHu().getYongHuDM() == yongHu.getYongHuDM()) {
				// brpm = Integer.parseInt(sqzlfmr.getPaiXu());
				// }
				// }
				// if(sqzlfmr.getShiFouWDW()) {
				// continue;
				// }
				// YongHu yonghu1 = (YongHu) sess.load(YongHu.class,
				// sqzlfmr.getYongHu().getYongHuDM());
				// if(yonghu1.getYongHuZT().getYongHuZTDM() != 2) {
				// }
				// dfpm +=1;
				// }
				// }
				
			}
		}

	}

	// 申报软件著作权
	private void addRuanJianZZQ(HibernateSession sess, ZhuanJiaSBSQ zhuanJiaSBSQ, YongHu yongHu, KaiQiSB kaiQiSB) {
		@SuppressWarnings("unchecked")
		List<RuanJianZZQ> Rjzzq = sess.createCriteria(RuanJianZZQ.class).add(Restrictions.sqlRestriction(
				"processinstanceended=1 and shanchuzt=0 and processinstanceterminated=0 and  this_.ruanJianZZQDM in (select distinct(fml.RuanJianZZQSQDM) from TECH_RuanJianZZQSQSJRY fml where fml.yonghudm = "
						+ yongHu.getYongHuDM() + ")" + " and this_.dengJiRQ  between to_date( '"
						+ getLatestNYears(kaiQiSB.getShenBaoKSSJ(), 6).getString("kaiShiSJYear")
						+ "-01-01 00:00:01', 'yyyy-mm-dd hh24:mi:ss')" + " and to_date('"
						+ getLatestNYears(kaiQiSB.getShenBaoKSSJ(), 6).getString("jieShuSJ")
						+ " 00:00:01', 'yyyy-mm-dd hh24:mi:ss') "))
				.addOrder(Order.asc("ruanJianZZQDM")).list();

		if (Rjzzq.size() > 0) {
			//获取当前专家申报功能中已经存在的软件著作权集合数据
			List<ShenBaoRJZZQ> xcShenBaoRJZZQs = sess.createCriteria(ShenBaoRJZZQ.class)
					.add(Restrictions.eq("zhuanJiaSBSQ.id", zhuanJiaSBSQ.getZhuanJiaSBSQDM())).list();
//			if (zhuanJiaSBSQ.getRjzzqs().size() > 0) {
//				zhuanJiaSBSQ.getRjzzqs().clear();
//			}
			for (RuanJianZZQ zzq : Rjzzq) {

				@SuppressWarnings("unchecked")
				// 先获取申报人在著作权中设计人员的排名，再去把该排名之前的人员筛选党管干部，减去这类之后就是得分排名
				List<RuanJianZZQSQSJRY> RuanJianZZQSQSJRYs = sess.createCriteria(RuanJianZZQSQSJRY.class)// 李睿帮助修改错误名称
						.add(Restrictions
								.sqlRestriction(" this_.RuanJianZZQSQDM =" + zzq.getRuanJianZZQSQ().getRuanJianZZQSQDM()
										+ " and this_.yongHuDM = " + yongHu.getYongHuDM() + ""))
						.addOrder(Order.asc("ruanJianZZQSQSJRYDM"))// 李睿帮助修改错误名称
						.list();

				Integer brpm = Integer.parseInt(RuanJianZZQSQSJRYs.get(0).getPaiMing());// 获得本人排名

				// 获取申请人排名之前，第一名之后的所有党管干部
				List<RuanJianZZQSQSJRY> rzsjryqm = sess.createCriteria(RuanJianZZQSQSJRY.class)// 李睿帮助修改错误名称
						.add(Restrictions.sqlRestriction(
								" this_.RuanJianZZQSQDM =" + zzq.getRuanJianZZQSQ().getRuanJianZZQSQDM() + ""))
						.add(Restrictions.lt("paiMing", RuanJianZZQSQSJRYs.get(0).getPaiMing()))// 排名数值小于申报人排名的所有数据
						.add(Restrictions.gt("paiMing", "1"))// 排名第一名的人员，无论党管干部也要算到得分排名中
						.add(Restrictions.eq("shiFouDGGB", true))// 是否党管干部
						.addOrder(Order.asc("ruanJianZZQSQSJRYDM"))// 李睿帮助修改错误名称
						.list();

				Integer dfpm = brpm - rzsjryqm.size();


				// 李睿帮助增加了一下软件著作权人
				List<RuanJianZZQSQRZQR> RuanJianZZQSQRZQRs = sess.createCriteria(RuanJianZZQSQRZQR.class)
						.add(Restrictions.sqlRestriction(
								" this_.RuanJianZZQSQDM =" + zzq.getRuanJianZZQSQ().getRuanJianZZQSQDM() + ""))
						.addOrder(Order.asc("ruanJianZZQSQRZQRDM")).list();

				String zzqr = "";
				// 李睿帮助增加了一下软件著作权人
				if (RuanJianZZQSQRZQRs.size() > 0) {
					for (int i = 0; i < RuanJianZZQSQRZQRs.size(); i++) {
						RuanJianZZQSQRZQR fmr = RuanJianZZQSQRZQRs.get(i);
						zzqr += fmr.getRuanZhuQRMC();

					}
				}
				int ycz = 0;// 标记现在这个满足条件的科技项目是否在验收科技立项项目集合中。
				for (ShenBaoRJZZQ xyrjzzqsqs : xcShenBaoRJZZQs) {
					if (xyrjzzqsqs.getRuanJianZZQ().getRuanJianZZQDM() == zzq.getRuanJianZZQDM()) {

						xyrjzzqsqs.setRuanJianZZQ(zzq);// 软件著作权
						xyrjzzqsqs.setDengJiH(zzq.getDengJiH());// 登记号
						xyrjzzqsqs.setDengJiR(zzq.getDengJiRQ());// 登记日
//						xyrjzzqsqs.setJiSuanDFPM(dfpm);// 得分排名
						xyrjzzqsqs.setShouQuanPM(brpm);// 授权排名
						xyrjzzqsqs.setZhuZuoQR(zzqr);// 著作权人
						ycz = 1;
						break;
					}
				}
				
				if(ycz == 0){

					ShenBaoRJZZQ ShenBaoRJZZQ = new ShenBaoRJZZQ();
					ShenBaoRJZZQ.assignPrimaryKey();

//					RuanJianZZQSQ sq = (RuanJianZZQSQ) sess.load(RuanJianZZQSQ.class,
//							zzq.getRuanJianZZQSQ().getRuanJianZZQSQDM());
					ShenBaoRJZZQ.setRuanJianZZQ(zzq);// 软件著作权
					ShenBaoRJZZQ.setDengJiH(zzq.getDengJiH());// 登记号
					ShenBaoRJZZQ.setDengJiR(zzq.getDengJiRQ());// 登记日
					ShenBaoRJZZQ.setJiSuanDFPM(dfpm);// 得分排名
					ShenBaoRJZZQ.setShouQuanPM(brpm);// 授权排名
					ShenBaoRJZZQ.setZhuZuoQR(zzqr);// 著作权人
					ShenBaoRJZZQ.setZhuanJiaSBSQ(zhuanJiaSBSQ);
					zhuanJiaSBSQ.getRjzzqs().add(ShenBaoRJZZQ);
				}
				
				/*
				 * 以下方式不太好，弃用 if(rzsjryqm.size()>0) { for (int i = 0 ; i <rzsjryqm.size() ; i
				 * ++) { RuanJianZZQSQSJRY pmqmr = rzsjryqm.get(i); if(pmqmr.getShiFouDGGB() ==
				 * true) { //制度中要求只有党管干部不参与排名，其他仍然需要 dfpm = brpm-1; }
				 * 
				 * 
				 * } }
				 */
			}
		}

	}

	// 发明专利申报
	private void addFaMingZLSB(HibernateSession sess, ZhuanJiaSBSQ zhuanJiaSBSQ, YongHu yongHu, KaiQiSB kaiQiSB) {
		@SuppressWarnings("unchecked")
		List<ZhuanLi> zhuanLis = sess.createCriteria(ZhuanLi.class).add(Restrictions.sqlRestriction(
				"processinstanceended=1 and shanchuzt=0 and processinstanceterminated=0 and  this_.zhuanlilxdm = 4 and this_.zhuanLiDM in (select distinct(fml.zhuanLiDM) from TECH_ZhuanLiZLFMR fml where fml.yongHuDM = '"
						+ yongHu.getYongHuDM() + "')" + " and this_.shouQuanGGR  between to_date( '"
						+ getLatestNYears(kaiQiSB.getShenBaoKSSJ(), 6).getString("kaiShiSJYear")
						+ "-01-01 00:00:01', 'yyyy-mm-dd hh24:mi:ss')" + " and to_date('"
						+ getLatestNYears(kaiQiSB.getShenBaoKSSJ(), 6).getString("jieShuSJ")
						+ " 00:00:01', 'yyyy-mm-dd hh24:mi:ss') "))
				.addOrder(Order.asc("zhuanLiDM")).list();

		if (zhuanLis.size() > 0) {
			//获取当前专家申报功能中已经存在的发明专利集合数据
			List<FaMingZLSB> xcfaMingZLSBs = sess.createCriteria(FaMingZLSB.class)
					.add(Restrictions.eq("zhuanJiaSBSQ.id", zhuanJiaSBSQ.getZhuanJiaSBSQDM())).list();
			
//			if (zhuanJiaSBSQ.getFmzlkhs().size() > 0) {
//				zhuanJiaSBSQ.getFmzlkhs().clear();
//			}
			for (ZhuanLi li : zhuanLis) {

				@SuppressWarnings("unchecked")
				// 李睿：先获取申报人在专利发明人员的排名，再去把该排名之前的人员筛选党管干部，减去这类之后就是得分排名
				List<ZhuanLiZLFMR> zhuanLiZLFMRs = sess.createCriteria(ZhuanLiZLFMR.class)
						.add(Restrictions.sqlRestriction(" this_.zhuanLiDM =" + li.getZhuanLiDM()
								+ "and this_.yongHuDM = " + yongHu.getYongHuDM() + ""))
						.addOrder(Order.asc("zhuanLiZLFMRDM")).list();
				
				Integer brpm = Integer.parseInt(zhuanLiZLFMRs.get(0).getPaiXu());// 获得本人排名

				// 李睿：获取申请人排名之前，第一名之后的所有党管干部
				List<ZhuanLiZLFMR> dggbsl = sess.createCriteria(ZhuanLiZLFMR.class)
//						.add(Restrictions.sqlRestriction(" this_.zhuanLiSQDM =" + li.getZhuanLiSQ().getZhuanLiSQDM() + ""))
						.add(Restrictions.lt("paiXu", zhuanLiZLFMRs.get(0).getPaiXu()))// 排名数值小于申报人排名的所有数据
						.add(Restrictions.gt("paiXu", "1"))// 排名第一名的人员，无论党管干部也要算到得分排名中
						.add(Restrictions.eq("shiFouDGGB", true))// 是否党管干部
						.addOrder(Order.asc("zhuanLiZLFMRDM"))// 李睿帮助修改错误名称
						.list();

				Integer dfpm = brpm - dggbsl.size();

				int ycz = 0;// 标记现在这个满足条件的发明专利是否在集合中。
				for (FaMingZLSB xcfmzl : xcfaMingZLSBs) {
					if (xcfmzl.getFaMingZL().getZhuanLiDM() == li.getZhuanLiDM()) {
						
						xcfmzl.setFaMingZL(li);// 专利
						xcfmzl.setShouQuanGGR(li.getShouQuanGGR());// 授权公告日
						xcfmzl.setZhuanLiGB(li.getZhuanLiGB());// 国别
						xcfmzl.setZhuanLiH(li.getZhuanLiH());// 专利号
						xcfmzl.setZhuanLiQRWB(li.getZhuanLiQRWB());// 专利权人文本
//						xcfmzl.setDeFenJSPM(dfpm);// 得分排名     得分排名允许用户自己填，第一次带出后，后面不再带出
						xcfmzl.setShouQuanPM(brpm);// 授权排名
						xcfmzl.setZhiShiCQLX(li.getZhuanLiLX());// 专利类型
						ycz = 1;
						break;
					}
				}
				
				if(ycz == 0){

					FaMingZLSB faMingZLSB = new FaMingZLSB();
					faMingZLSB.assignPrimaryKey();

					faMingZLSB.setFaMingZL(li);// 专利
					faMingZLSB.setShouQuanGGR(li.getShouQuanGGR());// 授权公告日
					faMingZLSB.setZhuanLiGB(li.getZhuanLiGB());// 国别
					faMingZLSB.setZhuanLiH(li.getZhuanLiH());// 专利号
					faMingZLSB.setZhuanLiQRWB(li.getZhuanLiQRWB());// 专利权人文本
					faMingZLSB.setDeFenJSPM(dfpm);// 得分排名
					faMingZLSB.setShouQuanPM(brpm);// 授权排名
					faMingZLSB.setZhiShiCQLX(li.getZhuanLiLX());// 专利类型
					faMingZLSB.setZhuanJiaSBSQ(zhuanJiaSBSQ);
					zhuanJiaSBSQ.getFmzlkhs().add(faMingZLSB);
				}
				// if(zhuanLiSQZLFMRs.size()>0) {
				// for (int i = 0 ; i <zhuanLiSQZLFMRs.size() ; i ++) {
				// ZhuanLiSQZLFMR sqzlfmr = zhuanLiSQZLFMRs.get(i);
				// if(sqzlfmr.getYongHu() != null) {
				// if(sqzlfmr.getYongHu().getYongHuDM() == yongHu.getYongHuDM()) {
				// brpm = Integer.parseInt(sqzlfmr.getPaiXu());
				// }
				// }
				// if(sqzlfmr.getShiFouWDW()) {
				// continue;
				// }
				// YongHu yonghu1 = (YongHu) sess.load(YongHu.class,
				// sqzlfmr.getYongHu().getYongHuDM());
				// if(yonghu1.getYongHuZT().getYongHuZTDM() != 2) {
				// }
				// dfpm +=1;
				// }
				// }

			}
		}

	}

	// 标准规范发布实施申报
	private void addBiaoZhunGFFBSSSB(HibernateSession sess, ZhuanJiaSBSQ zhuanJiaSBSQ, YongHu yongHu, KaiQiSB kaiQiSB) {
		@SuppressWarnings("unchecked")
		// 以下sql部分判断条件中取值原先取了标准规范立项的值，李睿改成了标准规范发布实施
		List<BiaoZhunGFFBSS> biaoZhunGFFBSSs = sess.createCriteria(BiaoZhunGFFBSS.class)
				.add(Restrictions.sqlRestriction(
						"processinstanceended=1 and shanchuzt=0 and processinstanceterminated=0 and  this_.biaoZhunGFFBSSDM in (select r.biaoZhunGFFBSSDM from TECH_BiaoZhunGFFBSSBZRY r where r.yonghudm= '"
								+ yongHu.getYongHuDM() + "') " + " and this_.faBuSJ  between to_date( '"
								+ getLatestNYears(kaiQiSB.getShenBaoKSSJ(), 6).getString("kaiShiSJYear")
								+ "-01-01 00:00:01', 'yyyy-mm-dd hh24:mi:ss')" + " and to_date('"
								+ getLatestNYears(kaiQiSB.getShenBaoKSSJ(), 6).getString("jieShuSJ")
								+ " 00:00:01', 'yyyy-mm-dd hh24:mi:ss') "))
				.addOrder(Order.asc("biaoZhunGFFBSSDM")).list();

		if (biaoZhunGFFBSSs.size() > 0) {

			//获取当前专家申报功能中已经存在的标准规范发布实施申报集合数据
			List<BiaoZhunGFFBSSSB> xcBiaoZhunGFFBSSSBs = sess.createCriteria(BiaoZhunGFFBSSSB.class)
					.add(Restrictions.eq("zhuanJiaSBSQ.id", zhuanJiaSBSQ.getZhuanJiaSBSQDM())).list();
//			if (zhuanJiaSBSQ.getBzgffbsskhs().size() > 0) {
//				zhuanJiaSBSQ.getBzgffbsskhs().clear();
//			}
			for (BiaoZhunGFFBSS biaoZhunGFFBSS : biaoZhunGFFBSSs) {
				@SuppressWarnings("unchecked")						
				List<BiaoZhunGFFBSSBZRY> biaoZhunGFLXBZRYs = sess.createCriteria(BiaoZhunGFFBSSBZRY.class)
						.add(Restrictions.sqlRestriction(
								" this_.biaoZhunGFFBSSDM =" + biaoZhunGFFBSS.getBiaoZhunGFFBSSDM() + "and this_.yongHuDM = " + yongHu.getYongHuDM() + ""))
						.addOrder(Order.asc("biaoZhunGFFBSSBZRYDM")).list();

				Integer brpm = Integer.parseInt(biaoZhunGFLXBZRYs.get(0).getPaiMing());// 获得本人排名;
				Integer dfpm = 0;

				int ycz = 0;// 标记现在这个满足条件的科技项目是否在验收科技立项项目集合中。
				for (BiaoZhunGFFBSSSB xybzs : xcBiaoZhunGFFBSSSBs) {
					if (xybzs.getBiaoZhunGFFBSS().getBiaoZhunGFFBSSDM() == biaoZhunGFFBSS.getBiaoZhunGFFBSSDM()) {
						// 标准规范发布实施
						xybzs.setSuoShuJB(biaoZhunGFFBSS.getSuoShuJB());// 所属级别
						xybzs.setBiaoZhunXZ(biaoZhunGFFBSS.getBiaoZhunXZ());// 标准性质
						xybzs.setXiangMuJSSJ(biaoZhunGFFBSS.getXiangMuJSSJ());// 项目结束时间
						xybzs.setXiangMuKSSJ(biaoZhunGFFBSS.getXiangMuKSSJ());// 项目开始时间
						xybzs.setShiShiSJ(biaoZhunGFFBSS.getShiShiSJ());// 实施时间
						xybzs.setChengDanDW(yongHu.getBuMen());//承担单位
						xybzs.setDanWeiJS(biaoZhunGFFBSS.getDanWeiJS());//单位角色
						xybzs.setBiaoZhunH(biaoZhunGFFBSS.getBiaoZhunBH());// 标准编号
						xybzs.setFaBuSJ(biaoZhunGFFBSS.getFaBuSJ());// 发布时间
						xybzs.setBenRenXMPM(brpm);//本人排名
						xybzs.setDanWeiJS(biaoZhunGFFBSS.getDanWeiJS());
						ycz = 1;
						break;
					}
				}
				
				if(ycz == 0){
					BiaoZhunGFFBSSSB biaoZhunGFFBSSSB = new BiaoZhunGFFBSSSB();
					biaoZhunGFFBSSSB.assignPrimaryKey();
					biaoZhunGFFBSSSB.setBiaoZhunGFFBSS(biaoZhunGFFBSS);
					// 标准规范发布实施
					biaoZhunGFFBSSSB.setSuoShuJB(biaoZhunGFFBSS.getSuoShuJB());// 所属级别
					biaoZhunGFFBSSSB.setBiaoZhunXZ(biaoZhunGFFBSS.getBiaoZhunXZ());// 标准性质
					biaoZhunGFFBSSSB.setXiangMuJSSJ(biaoZhunGFFBSS.getXiangMuJSSJ());// 项目结束时间
					biaoZhunGFFBSSSB.setXiangMuKSSJ(biaoZhunGFFBSS.getXiangMuKSSJ());// 项目开始时间
					biaoZhunGFFBSSSB.setShiShiSJ(biaoZhunGFFBSS.getShiShiSJ());// 实施时间
					biaoZhunGFFBSSSB.setChengDanDW(yongHu.getBuMen());
					biaoZhunGFFBSSSB.setBiaoZhunH(biaoZhunGFFBSS.getBiaoZhunBH());// 标准编号
					biaoZhunGFFBSSSB.setFaBuSJ(biaoZhunGFFBSS.getFaBuSJ());// 发布时间
					biaoZhunGFFBSSSB.setDanWeiJS(biaoZhunGFFBSS.getDanWeiJS());//单位角色


					// biaoZhunGFFBSSSB.setDeFenJSPM(dfpm);//得分排名
					biaoZhunGFFBSSSB.setDanWeiJS(biaoZhunGFFBSS.getDanWeiJS());
					biaoZhunGFFBSSSB.setBenRenXMPM(brpm);
					biaoZhunGFFBSSSB.setZhuanJiaSBSQ(zhuanJiaSBSQ);

					zhuanJiaSBSQ.getBzgffbsskhs().add(biaoZhunGFFBSSSB);
				}
				// if(biaoZhunGFLXBZRYs.size()>0) {
				// for (int i = 0 ; i <biaoZhunGFLXBZRYs.size() ; i ++) {
				// BiaoZhunGFLXBZRY zhunGFLXBZRY = biaoZhunGFLXBZRYs.get(i);
				// if(zhunGFLXBZRY.getYongHu() != null) {
				// if(zhunGFLXBZRY.getYongHu().getYongHuDM() == yongHu.getYongHuDM()) {
				// brpm = Integer.parseInt(zhunGFLXBZRY.getPaiMing());
				// }
				// }
				// if(zhunGFLXBZRY.getShiFouWDW()) {
				// continue;
				// }
				// YongHu yonghu1 = (YongHu) sess.load(YongHu.class,
				// zhunGFLXBZRY.getYongHu().getYongHuDM());
				// if(yonghu1.getYongHuZT().getYongHuZTDM() != 2) {
				// }
				// dfpm +=1;
				// }
				// }
				// 以下四行暂时没用，还报错，李睿注释
				// @SuppressWarnings("unchecked")
				// List<BiaoZhunGFLXZBDW> zhunGFLXZBDWs =
				// sess.createCriteria(BiaoZhunGFLXZBDW.class)
				// .add(Restrictions.sqlRestriction(" this_.biaoZhunGFLXDM
				// ="+biaoZhunGFFBSS.getBiaoZhunGFLX().getBiaoZhunGFLXDM()+" and this_.bumendm =
				// "+yongHu.getYongHuDM()+""))
				// .addOrder(Order.asc("biaoZhunGFLXZBDWDM"))
				// .list();
			}
		}

	}

	// 验收科技立项项目
	private void addyanShouKJLXXMSBSB(HibernateSession sess, ZhuanJiaSBSQ zhuanJiaSBSQ, YongHu yongHu,
			KaiQiSB kaiQiSB) {
		// 需优化为使用用户做关联 不能使用
		@SuppressWarnings("unchecked")
		List<KeJiLXXM> jiLXXMsys = sess.createCriteria(KeJiLXXM.class).add(Restrictions.sqlRestriction(
				"processinstanceended=1 and shanchuzt=0 and processinstanceterminated=0 and this_.keJiLXXMdm in (select distinct(jh.kejilxxmdm) from TECH_KeTiZCYQKJH jh where jh.yonghudm='"
						+ yongHu.getYongHuDM() + "') " + " and this_.shijijtyssj  between to_date( '"
						+ getLatestNYears(kaiQiSB.getShenBaoKSSJ(), 6).getString("kaiShiSJYear")
						+ "-01-01 00:00:01', 'yyyy-mm-dd hh24:mi:ss')" + " and to_date('"
						+ getLatestNYears(kaiQiSB.getShenBaoKSSJ(), 6).getString("jieShuSJ")
						+ " 00:00:01', 'yyyy-mm-dd hh24:mi:ss') " + " and this_.xiangmuztdm =4"))
				.addOrder(Order.asc("keJiLXXMDM")).list();

		if (jiLXXMsys.size() > 0) {
			//获取当前专家申报功能中已经存在的验收科技立项项目集合数据
			List<YanShouKJLXXMSB> YanShouKJLXXMSBs = sess.createCriteria(YanShouKJLXXMSB.class)
					.add(Restrictions.eq("zhuanJiaSBSQ.id", zhuanJiaSBSQ.getZhuanJiaSBSQDM())).list();
//			if (zhuanJiaSBSQ.getYskjlxxms().size() > 0) {
//				 zhuanJiaSBSQ.getYskjlxxms().clear();
//			}
			for (KeJiLXXM keJiLXXM : jiLXXMsys) {

				@SuppressWarnings("unchecked")
				// 李睿增加，获取项目课题组成员中的排名
				List<KeTiZCYQKJH> keTiZCYQKJHs = sess.createCriteria(KeTiZCYQKJH.class)
						.add(Restrictions.sqlRestriction(" this_.keJiLXXMDM =" + keJiLXXM.getKeJiLXXMDM() + ""))
						.add(Restrictions.eq("yongHu.id", yongHu.getYongHuDM())).addOrder(Order.asc("paiMing")).list();

				Integer brpm = 0;
				if(keTiZCYQKJHs.get(0).getPaiMing()!=null){
					brpm = Integer.parseInt(keTiZCYQKJHs.get(0).getPaiMing());
				}
						
				Integer dfpm = 0;
				@SuppressWarnings("unchecked")
				//李睿：著作权需要满足两个条件：（1）申请的专家作为著作权开发人员，（2）科技项目关联到的软件著作权
				List<RuanJianZZQ> RuanJianZZQs = sess.createCriteria(RuanJianZZQ.class).add(Restrictions.sqlRestriction(
						"this_.RuanJianZZQSQDM in (select distinct(fml.RuanJianZZQSQDM) from TECH_RuanJianZZQSQSJRY fml where fml.yonghudm = "
						+ yongHu.getYongHuDM() + ") and this_.RuanJianZZQDM in (select glzzq.ruanjianzzqdm from tech_yukejxmgldrjzzq glzzq where glzzq.processinstanceended=1 and glzzq.shanchuzt=0 and glzzq.processinstanceterminated=0 and glzzq.kejilxxmdm ="+ keJiLXXM.getKeJiLXXMDM() + " )"))
						.list();

				@SuppressWarnings("unchecked")
				//李睿：专利需要满足两个条件：（1）申请的专家作为专利发明人，（2）科技项目关联到的专利
				List<ZhuanLi> ZhuanLis = sess.createCriteria(ZhuanLi.class).add(Restrictions.sqlRestriction(
						"this_.zhuanLiDM in (select distinct(fml.zhuanLiDM) from TECH_ZhuanLiZLFMR fml where fml.yonghudm = '"
						+ yongHu.getYongHuDM() + "') and  this_.zhuanLiDM in (select glzl.zhuanlidm  from tech_yukejxmgldsqzl glzl  where glzl.processinstanceended = 1  and glzl.shanchuzt = 0 and glzl.processinstanceterminated=0 and glzl.kejilxxmdm="+ keJiLXXM.getKeJiLXXMDM() + " )"))
						.list();

				int ycz = 0;// 标记现在这个满足条件的科技项目是否在验收科技立项项目集合中。
				for (YanShouKJLXXMSB yanShouKJLXXMSb : YanShouKJLXXMSBs) {
					if (yanShouKJLXXMSb.getKeJiLXXM().getKeJiLXXMDM() == keJiLXXM.getKeJiLXXMDM()) {

						// yanShouKJLXXMSB.setKeJiLXXM(keJiLXXM);//科技立项项目
						yanShouKJLXXMSb.setXiangMuJB(keJiLXXM.getXiangMuJB());// 项目级别
						yanShouKJLXXMSb.setXiangMuKSSJ(keJiLXXM.getYanJiuKSNY());// 开始时间
						yanShouKJLXXMSb.setXiangMuJSSJ(keJiLXXM.getYanJiuJSNY());// 结束时间
						yanShouKJLXXMSb.setBenRenXMPM(brpm);// 本人项目排名
//						yanShouKJLXXMSb.setDeFenJSPM(brpm.toString());// 得分计算排名,
						yanShouKJLXXMSb.setTongGuoYSSJ(keJiLXXM.getShiJiJTYSSJ());// 验收时间
						yanShouKJLXXMSb.setZhuanJiaSBSQ(zhuanJiaSBSQ);
						yanShouKJLXXMSb.setChengDanDW(yongHu.getBuMen());
						yanShouKJLXXMSb.setXiangMuBH(keJiLXXM.getXiangMuBH());// 项目编号
						yanShouKJLXXMSb.setLiXiangND(keJiLXXM.getNianDu());// 立项年度
						// (3)有无知识产权(需确认取值)
						if (RuanJianZZQs.size() != 0 || ZhuanLis.size() != 0) {
							yanShouKJLXXMSb.setYouWuZSCQ(true);
							if(ZhuanLis.size()>0){
								boolean czfmzl = true; 
								boolean czsxyx = true; 

								for (ZhuanLi zl : ZhuanLis) {
									if(zl.getZhuanLiLX().getZhuanLiLXDM()==4 || zl.getZhuanLiLX().getZhuanLiLXDM()==5){//发明专利和PCT都属于发明专利
										yanShouKJLXXMSb.setZhuanLiLX((KeJiXMZLLX) sess.load(KeJiXMZLLX.class, 1));//发明专利
										yanShouKJLXXMSb.setZhuanLiXS(1.2);//专利系数

										czfmzl = false;
										break;
									}												
								}
								
								if(czfmzl){
									for (ZhuanLi zl : ZhuanLis) {
										if(zl.getZhuanLiLX().getZhuanLiLXDM()==3){
											yanShouKJLXXMSb.setZhuanLiLX((KeJiXMZLLX) sess.load(KeJiXMZLLX.class, 2));//实用新型专利
											yanShouKJLXXMSb.setZhuanLiXS(1.0);//专利系数
											czsxyx = false;
											break;
										}												
									}
								}
								if(czfmzl & czsxyx){//如果发明和实用新型都没有，那只剩下一个外观设计
									yanShouKJLXXMSb.setZhuanLiLX((KeJiXMZLLX) sess.load(KeJiXMZLLX.class, 4));//外观设计
									yanShouKJLXXMSb.setZhuanLiXS(1.0);//专利系数

								}
								
							}else{
								yanShouKJLXXMSb.setZhuanLiLX((KeJiXMZLLX) sess.load(KeJiXMZLLX.class, 3));//如果没有专利，那一定有软著
								yanShouKJLXXMSb.setZhuanLiXS(1.0);//专利系数
							}
						} else {
							yanShouKJLXXMSb.setYouWuZSCQ(false);
							yanShouKJLXXMSb.setZhuanLiLX((KeJiXMZLLX) sess.load(KeJiXMZLLX.class, 5));
							yanShouKJLXXMSb.setZhuanLiXS(0.5);//专利系数
						}

						if (keJiLXXM.getShiJiJTYSSJ() != null && keJiLXXM.getYanJiuJSNY() != null) {// 李睿：需要根据【研究结束日期】字段进行判断，而不是【结束日期】
							int yfc = getMonthDiff(keJiLXXM.getShiJiJTYSSJ(), keJiLXXM.getYanJiuJSNY());
							if (yfc >= 4 && yfc <= 6) {
								yanShouKJLXXMSb.setJinDuXS(0.8);
							} else if (yfc > 6 && yfc < 12) {
								yanShouKJLXXMSb.setJinDuXS(0.7);
							} else if (yfc >= 12 && yfc < 18) {
								yanShouKJLXXMSb.setJinDuXS(0.6);
							} else if (yfc >= 18) {
								yanShouKJLXXMSb.setJinDuXS(0.5);
							} else {
								yanShouKJLXXMSb.setJinDuXS(1.0);
							}
						}
						ycz = 1;
						break;
					}
				}
				if (ycz == 0) {
					YanShouKJLXXMSB yanShouKJLXXMSB = new YanShouKJLXXMSB();
					yanShouKJLXXMSB.assignPrimaryKey();

					// if(keTiZCYQKJHs.size()>0) {
					// for (int i = 0 ; i <keTiZCYQKJHs.size() ; i ++) {
					// KeTiZCYQKJH keTiZCYQKJH = keTiZCYQKJHs.get(i);
					// if(keTiZCYQKJH.getYongHu() != null) {
					// if(keTiZCYQKJH.getYongHu().getYongHuDM() == yongHu.getYongHuDM()) {
					// brpm = Integer.parseInt(keTiZCYQKJH.getPaiMing());
					// }
					//
					// YongHu yonghu1 = (YongHu) sess.load(YongHu.class,
					// keTiZCYQKJH.getYongHu().getYongHuDM());
					// if(yonghu1.getYongHuZT().getYongHuZTDM() != 2) {
					// continue;
					// }
					//
					// }
					// if(keTiZCYQKJH.getShiFouWDW()) {
					// continue;
					// }
					//
					// dfpm +=1;
					// }
					// }

					yanShouKJLXXMSB.setKeJiLXXM(keJiLXXM);// 科技立项项目
					yanShouKJLXXMSB.setXiangMuJB(keJiLXXM.getXiangMuJB());// 项目级别
					yanShouKJLXXMSB.setXiangMuKSSJ(keJiLXXM.getYanJiuKSNY());// 开始时间
					yanShouKJLXXMSB.setXiangMuJSSJ(keJiLXXM.getYanJiuJSNY());// 结束时间
					yanShouKJLXXMSB.setBenRenXMPM(brpm);// 本人项目排名
					yanShouKJLXXMSB.setDeFenJSPM(brpm.toString());// 得分计算排名，暂时先设置为本人排名，后续待党管干部库完善后再设置
					yanShouKJLXXMSB.setTongGuoYSSJ(keJiLXXM.getShiJiJTYSSJ());// 验收时间
					yanShouKJLXXMSB.setZhuanJiaSBSQ(zhuanJiaSBSQ);
					yanShouKJLXXMSB.setChengDanDW(yongHu.getBuMen());
					yanShouKJLXXMSB.setXiangMuBH(keJiLXXM.getXiangMuBH());// 项目编号
					yanShouKJLXXMSB.setLiXiangND(keJiLXXM.getNianDu());// 立项年度
					// (3)有无知识产权(需确认取值)
					if (RuanJianZZQs.size() != 0 || ZhuanLis.size() != 0) {
						yanShouKJLXXMSB.setYouWuZSCQ(true);
						if(ZhuanLis.size()>0){
							boolean czfmzl = true; 
							boolean czsxyx = true; 

							for (ZhuanLi zl : ZhuanLis) {
								if(zl.getZhuanLiLX().getZhuanLiLXDM()==4 || zl.getZhuanLiLX().getZhuanLiLXDM()==5){//发明专利和PCT都属于发明专利
									yanShouKJLXXMSB.setZhuanLiLX((KeJiXMZLLX) sess.load(KeJiXMZLLX.class, 1));//发明专利
									yanShouKJLXXMSB.setZhuanLiXS(1.2);//专利系数
									czfmzl = false;
									break;
								}												
							}
							
							if(czfmzl){
								for (ZhuanLi zl : ZhuanLis) {
									if(zl.getZhuanLiLX().getZhuanLiLXDM()==3){
										yanShouKJLXXMSB.setZhuanLiLX((KeJiXMZLLX) sess.load(KeJiXMZLLX.class, 2));//实用新型专利
										yanShouKJLXXMSB.setZhuanLiXS(1.0);//专利系数
										czsxyx = false;
										break;
									}												
								}
							}
							if(czfmzl & czsxyx){//如果发明和实用新型都没有，那只剩下一个外观设计
								yanShouKJLXXMSB.setZhuanLiLX((KeJiXMZLLX) sess.load(KeJiXMZLLX.class, 4));//外观设计
								yanShouKJLXXMSB.setZhuanLiXS(1.0);//专利系数
							}
							
						}else{
							yanShouKJLXXMSB.setZhuanLiLX((KeJiXMZLLX) sess.load(KeJiXMZLLX.class, 3));//如果没有专利，那一定有软著
							yanShouKJLXXMSB.setZhuanLiXS(1.0);//专利系数
						}
					} else {
						yanShouKJLXXMSB.setYouWuZSCQ(false);
						yanShouKJLXXMSB.setZhuanLiLX((KeJiXMZLLX) sess.load(KeJiXMZLLX.class, 5));
						yanShouKJLXXMSB.setZhuanLiXS(0.5);//专利系数
					}

					if (keJiLXXM.getShiJiJTYSSJ() != null && keJiLXXM.getYanJiuJSNY() != null) {
						int yfc = getMonthDiff(keJiLXXM.getShiJiJTYSSJ(), keJiLXXM.getYanJiuJSNY());
						if (yfc >= 4 && yfc <= 6) {
							yanShouKJLXXMSB.setJinDuXS(0.8);
						} else if (yfc > 6 && yfc < 12) {
							yanShouKJLXXMSB.setJinDuXS(0.7);
						} else if (yfc >= 12 && yfc < 18) {
							yanShouKJLXXMSB.setJinDuXS(0.6);
						} else if (yfc >= 18) {
							yanShouKJLXXMSB.setJinDuXS(0.5);
						} else {
							yanShouKJLXXMSB.setJinDuXS(1.0);
						}
					}
					zhuanJiaSBSQ.getYskjlxxms().add(yanShouKJLXXMSB);
				}
			}
		}
	}

	private Integer Integer(String paiMing) {
		// TODO Auto-generated method stub
		return null;
	}

	// 通用获取近n年的年份
	public JSONObject getLatestNYears(Date date, int years) {// 获取年份数
		// 与李睿确认 当开启时间为2020年3月15日 且取六年的数据 则时间范围为 2014年01月01日至2020年3月14日的全部数据
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat sdfYear = new SimpleDateFormat("yyyy");
		JSONObject object = new JSONObject();
		object.put("kaiShiSJYear", (Integer.parseInt(sdfYear.format(date)) - years));
		object.put("jieShuSJ", sdf.format(date));
		return object;
	}

	// 获取时间差
	public static int getMonthDiff(Date d1, Date d2) {
		Calendar c1 = Calendar.getInstance();
		Calendar c2 = Calendar.getInstance();
		c1.setTime(d1);
		c2.setTime(d2);
		int year1 = c1.get(Calendar.YEAR);
		int year2 = c2.get(Calendar.YEAR);
		int month1 = c1.get(Calendar.MONTH);
		int month2 = c2.get(Calendar.MONTH);
		int day1 = c1.get(Calendar.DAY_OF_MONTH);
		int day2 = c2.get(Calendar.DAY_OF_MONTH);
		// 获取年的差值 
		int yearInterval = year1 - year2;
		// 如果 d1的 月-日 小于 d2的 月-日 那么 yearInterval-- 这样就得到了相差的年数
		if (month1 < month2 || month1 == month2 && day1 < day2) {
			yearInterval--;
		}
		// 获取月数差值
		int monthInterval = (month1 + 12) - month2;
		if (day1 < day2) {
			monthInterval--;
		}
		monthInterval %= 12;
		int monthsDiff = Math.abs(yearInterval * 12 + monthInterval);
		return monthsDiff;

	}

}
