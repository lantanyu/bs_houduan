package com.example.orderservice.controller;

import com.example.commons.po.*;
import com.example.orderservice.Config.RabbitMQConfig;
import com.example.orderservice.Listener.chashong;
import com.example.orderservice.service.OrderService;
import com.example.orderservice.service.Productservice;
import com.example.orderservice.service.RedisService1;
import com.example.orderservice.service.Userservice;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigInteger;
import java.util.List;

@Slf4j
@CrossOrigin
@RestController
@RequestMapping("/order")
public class OrderController {
    @Autowired
    private OrderService orderService;
    @Autowired
    private Productservice productservice;
    @Autowired
    private Userservice userservice;
    @Autowired
    private RedisService1<cuser> redisService;
    @Autowired
    chashong chashong;
    @Autowired
    private  ApplicationContext applicationContext;



    @GetMapping("/shiyan")
    public CommonResult shiyan() {
        return productservice.shiyan();
    }

    @GetMapping("/seeorder")
    public CommonResult seeorder(){
        return new  CommonResult<String> (200,"sdsd",productservice.ifproductstatus(new BigInteger("1")));
    }
//    @PostMapping("/createorder/{addressid}/{productid}")
//    public CommonResult createorder(@PathVariable(value = "addressid") BigInteger addressid, @PathVariable(value = "productid") BigInteger productid, HttpServletRequest request){
//        product product = productservice.updataifproductstatus(productid,1);
//
//        if(product==null){
//            return new  CommonResult<String> (200,"???????????????","???????????????");
//        }
//        CommonResult<address> commonResult = userservice.getaddressbyid(addressid);
//        address address = commonResult.getData();
//        String token = request.getHeader("token");
//        cuser cuser = redisService.get(token);
//        order order = new order();
//        order.setProductid(productid);
//        order.setUserid(cuser.getUserid());
//        order.setSn(UUID.randomUUID()+"");
//        order.setBaoyouprice(product.getBaoyouprice());
//        order.setPrice(product.getPrice());
//        order.setProductname(product.getProductidname());
//        order.setProducticon(product.getPic());
//        order.setStatus(0);//0????????????1????????????2????????????3????????????4???????????????5????????????
//        order.setMethod(product.getMethod());
//        order.setAddressname(address.getName());
//        order.setProvince(address.getProvince());
//        order.setCity(address.getCity());
//        order.setRegion(address.getRegion());
//        order.setDetailaddresss(address.getDetailaddresss());
//        order.setPhone(address.getPhone());
//        order.setByuserid(product.getUserid());
//        Date date = new Date();
//        Timestamp timestamp = new Timestamp(date.getTime());
//        order.setTijiaotime(timestamp);
//        if(orderService.createorder(order)>0){
//            return new  CommonResult<order> (200,"????????????",order);
//        }
//        return new CommonResult(200,"????????????");
//    }
    @GetMapping("/getorder/{ifs}")
    public  CommonResult getorder(@PathVariable(value = "ifs") int ifs,HttpServletRequest request) {
        if(ifs!=1&&ifs!=2)return new CommonResult(400,"ifs?????????1???2");
        String token = request.getHeader("token");
        cuser cuser = redisService.get(token);
        return new  CommonResult<List<order>> (200,"????????????",orderService.getorder(cuser,ifs));
    }
    @GetMapping("getbyorder/{orderid}")
    public CommonResult getbyorder(@PathVariable(value = "orderid")BigInteger orderid) {
       return new CommonResult<byorder>(200,"??????",orderService.getbyorder(orderid));
    };

    @PostMapping("/createorder/{addressid}/{productid}")
    public CommonResult createorder(@PathVariable(value = "addressid") BigInteger addressid, @PathVariable(value = "productid") BigInteger productid, HttpServletRequest request) throws JsonProcessingException {
        order order = new order();
        int i = orderService.createorder(addressid,productid,request,order);
        if(i==0){
            return new  CommonResult<order> (400,"???????????????");
        }else if (i==1) {
            return new  CommonResult<order> (200,"????????????",order);
        }else {
            return new CommonResult(201,"????????????");
        }

    }

    @PostMapping("/zhifu/{orderid}")
    public CommonResult zhifu(@PathVariable(value = "orderid") BigInteger orderid,HttpServletRequest request){
        String token = request.getHeader("token");
        cuser cuser = redisService.get(token);

        if (orderService.zhufu(orderid,cuser.getUserid())>0){
            log.info("----?????????????????????????????????");
            return new  CommonResult(200,"????????????");
        }
        return new  CommonResult(400,"????????????");
    }
    @PostMapping("/fahuo")
    public CommonResult fahuo(@RequestBody order order,HttpServletRequest request){
        //{"orderid":2,"kuidihao":"sf0123456789","kuidi":"????????????"}
        String regex_name = "\\S{1,29}";
        if(order.getOrderid()==null)return new CommonResult(400,"?????????id");
        if(order.getKuidi()==null||!order.getKuidi().matches(regex_name))return new CommonResult(400,"???????????????????????????");
        if(order.getKuidihao()==null||!order.getKuidihao().matches(regex_name))return new CommonResult(400,"????????????????????????");
        String token = request.getHeader("token");
        cuser cuser = redisService.get(token);
        order.setByuserid(cuser.getUserid());
        if(orderService.fahuo(order)>0){
            return new  CommonResult(200,"????????????");
        }
        return new  CommonResult(400,"????????????");
    }
    @PostMapping("/queding/{orderid}")
    public CommonResult queding(@PathVariable(value = "orderid") BigInteger orderid,HttpServletRequest request) {
        String token = request.getHeader("token");
        cuser cuser = redisService.get(token);
        if(orderService.queding(orderid,cuser.getUserid())>0){
            return new  CommonResult(200,"????????????");
        }
        return new  CommonResult(400,"????????????");
    }
    @PostMapping("/tuihuo")
    public CommonResult tuihuo(@RequestBody byorder byorder,HttpServletRequest request) {
        //{"orderid":2,"yunprice":15,"methon":0,"tuihuotext":"????????????"}
        String regex_name = "\\S{1,100}";
        if(byorder.getOrderid()==null)return new CommonResult(400,"?????????id");
        if(byorder.getMethon()!=0&&byorder.getMethon()!=1)return new CommonResult(400,"methon????????????");
        if(byorder.getTuihuotext()==null||!byorder.getTuihuotext().matches(regex_name))return new CommonResult(400,"?????????????????????");
        if(byorder.getYunprice()<0||byorder.getYunprice()>100000)return new CommonResult(400,"?????????????????????");
        String token = request.getHeader("token");
        cuser cuser = redisService.get(token);
        int i = orderService.tuihuo(byorder,cuser);
        if(i==-3){
            return new CommonResult(400,"????????????");
        }
        if(i==-2){
            return new CommonResult(400,"????????????????????????");
        }
        if (i==-1){
            return new CommonResult(400,"???????????????????????????");
        }
        return new  CommonResult(200,"????????????");
    }
    @PostMapping("/yestuihuo/{addressid}/{byorderid}")
    public CommonResult yestuihuo(@PathVariable(value = "addressid") BigInteger addressid,@PathVariable(value = "byorderid") BigInteger byorderid,HttpServletRequest request){
        String token = request.getHeader("token");
        cuser cuser = redisService.get(token);
        int i = orderService.yestuihuo(addressid,byorderid,cuser);
        if (i==-1){
            return  new CommonResult(400,"????????????");
        }
        if(i>0){
            return new  CommonResult(200,"????????????");
        }
        return new CommonResult(400,"??????");
    }
    @PostMapping("/tuihuofahuo")
    public CommonResult tuihuofahuo(@RequestBody byorder byorder,HttpServletRequest request){
        //{"byorderid":1,"kuidihao":"sf0123456789","kuidi":"????????????"}
        String regex_name = "\\S{1,29}";
        if(byorder.getByorderid()==null)return new CommonResult(400,"?????????id");
        if(byorder.getKuidi()==null||!byorder.getKuidi().matches(regex_name))return new CommonResult(400,"???????????????????????????");
        if(byorder.getKuidihao()==null||!byorder.getKuidihao().matches(regex_name))return new CommonResult(400,"????????????????????????");
        String token = request.getHeader("token");
        cuser cuser = redisService.get(token);
        int i = orderService.tuihuofahuo(byorder,cuser);
        if(i==-1){
            return  new CommonResult(400,"????????????");
        }
        if (i>0){
            return new  CommonResult(200,"????????????");
        }
        return new CommonResult(400,"??????");
    }
    @PostMapping("/tuihuoqueding/{byorderid}")
    public CommonResult tuihuoqueding(@PathVariable(value = "byorderid") BigInteger byorderid,HttpServletRequest request){
        String token = request.getHeader("token");
        cuser cuser = redisService.get(token);
        int i = orderService.tuihuoqueding(byorderid,cuser);
        if(i==-2){
            return  new CommonResult(400,"????????????");
        }
        if (i>0){
            return new  CommonResult(200,"????????????");
        }
        return new CommonResult(400,"??????");
    }
    @PostMapping("/shiyan")
    public CommonResult shiyanrabbitmq(){
        orderService.shiyan();
        return null;
    }


}
